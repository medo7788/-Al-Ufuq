package com.example.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.utils.QiblaCalculator.calculateBearing
import com.example.utils.QiblaCalculator.calculateRelativeQiblaAngle
import com.example.utils.QiblaCalculator.getDeclination
import com.example.utils.QiblaCalculator.isAligned
import com.example.utils.QiblaCalculator.magneticToTrueHeading
import com.example.utils.QiblaCalculator.normalizeAngle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

enum class QiblaStatus {
    LOCATING,
    CALIBRATING,
    READY,
    SENSOR_UNAVAILABLE,
    PERMISSION_MISSING,
    LOCATION_UNAVAILABLE
}

data class QiblaState(
    val status: QiblaStatus = QiblaStatus.LOCATING,
    val qiblaBearing: Float = 136.1f,         // Kaaba initial bearing from North
    val deviceHeading: Float = 0f,            // Device azimuth in degrees relative to True North
    val relativeQiblaAngle: Float = 136.1f,   // Qibla direction relative to device top
    val isAligned: Boolean = false,           // Heading within ±5° of Qibla bearing
    val sensorAccuracy: Int = SensorManager.SENSOR_STATUS_ACCURACY_HIGH,
    val latitude: Double = 30.0444,
    val longitude: Double = 31.2357,
    val locationName: String = "القاهرة، مصر"
)

class QiblaSensorManager(context: Context) : SensorEventListener {

    private val sensorManager = context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    private var rotationVectorSensor: Sensor? = null
    private var geomagneticSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null
    private var magnetometerSensor: Sensor? = null

    private val gravityValues = FloatArray(3)
    private val geomagneticValues = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationValues = FloatArray(3)

    private var hasGravity = false
    private var hasGeomagnetic = false

    private var currentDeclination = 0f
    private var smoothedHeading = 0f

    private val _state = MutableStateFlow(QiblaState())
    val state: StateFlow<QiblaState> = _state.asStateFlow()

    init {
        sensorManager?.let { sm ->
            rotationVectorSensor = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
                ?: sm.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)
            accelerometerSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            magnetometerSensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        }

        if (rotationVectorSensor == null && (accelerometerSensor == null || magnetometerSensor == null)) {
            _state.value = _state.value.copy(status = QiblaStatus.SENSOR_UNAVAILABLE)
        }
    }

    fun startListening(lat: Double, lng: Double, locationName: String) {
        val qiblaBearing = calculateBearing(lat, lng)
        val declination = getDeclination(lat, lng)
        currentDeclination = declination

        _state.value = _state.value.copy(
            qiblaBearing = qiblaBearing,
            latitude = lat,
            longitude = lng,
            locationName = locationName,
            status = if (_state.value.status == QiblaStatus.SENSOR_UNAVAILABLE) QiblaStatus.SENSOR_UNAVAILABLE else QiblaStatus.LOCATING
        )

        sensorManager?.let { sm ->
            rotationVectorSensor?.let { sensor ->
                sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
                return
            }
            accelerometerSensor?.let { sm.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
            magnetometerSensor?.let { sm.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
        }
    }

    fun stopListening() {
        sensorManager?.unregisterListener(this)
    }

    fun updateLocation(lat: Double, lng: Double, locationName: String) {
        val qiblaBearing = calculateBearing(lat, lng)
        val declination = getDeclination(lat, lng)
        currentDeclination = declination

        val currentHeading = _state.value.deviceHeading
        val relativeAngle = calculateRelativeQiblaAngle(currentHeading, qiblaBearing)
        val aligned = isAligned(currentHeading, qiblaBearing)

        _state.value = _state.value.copy(
            qiblaBearing = qiblaBearing,
            relativeQiblaAngle = relativeAngle,
            isAligned = aligned,
            latitude = lat,
            longitude = lng,
            locationName = locationName
        )
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        var rawMagneticHeading = 0f
        var accuracy = event.accuracy

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR || event.sensor.type == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationValues)
            rawMagneticHeading = Math.toDegrees(orientationValues[0].toDouble()).toFloat()
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, gravityValues, 0, 3)
            hasGravity = true
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, geomagneticValues, 0, 3)
            hasGeomagnetic = true
        }

        if (hasGravity && hasGeomagnetic) {
            val success = SensorManager.getRotationMatrix(rotationMatrix, null, gravityValues, geomagneticValues)
            if (success) {
                SensorManager.getOrientation(rotationMatrix, orientationValues)
                rawMagneticHeading = Math.toDegrees(orientationValues[0].toDouble()).toFloat()
            }
        }

        val trueHeading = magneticToTrueHeading(rawMagneticHeading, currentDeclination)
        smoothedHeading = smoothAngle(smoothedHeading, trueHeading, 0.2f)

        val currentState = _state.value
        val relativeAngle = calculateRelativeQiblaAngle(smoothedHeading, currentState.qiblaBearing)
        val aligned = isAligned(smoothedHeading, currentState.qiblaBearing)

        val newStatus = when {
            accuracy <= SensorManager.SENSOR_STATUS_ACCURACY_LOW && accuracy != SensorManager.SENSOR_STATUS_UNRELIABLE -> QiblaStatus.CALIBRATING
            accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE -> QiblaStatus.CALIBRATING
            else -> QiblaStatus.READY
        }

        _state.value = currentState.copy(
            status = newStatus,
            deviceHeading = smoothedHeading,
            relativeQiblaAngle = relativeAngle,
            isAligned = aligned,
            sensorAccuracy = accuracy
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        val current = _state.value
        if (accuracy <= SensorManager.SENSOR_STATUS_ACCURACY_LOW) {
            _state.value = current.copy(status = QiblaStatus.CALIBRATING, sensorAccuracy = accuracy)
        } else if (current.status == QiblaStatus.CALIBRATING) {
            _state.value = current.copy(status = QiblaStatus.READY, sensorAccuracy = accuracy)
        }
    }

    /**
     * Exponential smoothing for angle transitions without circular boundary jumps.
     */
    private fun smoothAngle(current: Float, target: Float, alpha: Float): Float {
        var diff = target - current
        while (diff < -180f) diff += 360f
        while (diff > 180f) diff -= 360f
        return normalizeAngle(current + alpha * diff)
    }
}
