package com.example.utils

import android.hardware.GeomagneticField
import kotlin.math.*

object QiblaCalculator {

    const val KAABA_LATITUDE = 21.422487
    const val KAABA_LONGITUDE = 39.826206

    /**
     * Calculates the initial bearing (forward azimuth) from user location to Kaaba in Mecca.
     * Returns initial bearing in degrees in range [0, 360).
     */
    fun calculateBearing(userLat: Double, userLng: Double): Float {
        val userLatRad = Math.toRadians(userLat)
        val kaabaLatRad = Math.toRadians(KAABA_LATITUDE)
        val deltaLngRad = Math.toRadians(KAABA_LONGITUDE - userLng)

        val y = sin(deltaLngRad) * cos(kaabaLatRad)
        val x = cos(userLatRad) * sin(kaabaLatRad) - sin(userLatRad) * cos(kaabaLatRad) * cos(deltaLngRad)

        var bearingRad = atan2(y, x)
        var bearingDeg = Math.toDegrees(bearingRad)
        return normalizeAngle(bearingDeg.toFloat())
    }

    /**
     * Returns geomagnetic declination in degrees for user's location at current time.
     * Positive declination means Magnetic North is East of True North.
     */
    fun getDeclination(userLat: Double, userLng: Double, altitudeMeters: Double = 0.0): Float {
        val timeMillis = System.currentTimeMillis()
        val geoField = GeomagneticField(
            userLat.toFloat(),
            userLng.toFloat(),
            altitudeMeters.toFloat(),
            timeMillis
        )
        return geoField.declination
    }

    /**
     * Converts Magnetic Heading (measured by compass sensors relative to Magnetic North)
     * to True Heading (relative to True North) using local geomagnetic declination.
     */
    fun magneticToTrueHeading(magneticHeading: Float, declination: Float): Float {
        return normalizeAngle(magneticHeading + declination)
    }

    /**
     * Calculates relative angle in degrees [-180, 180) to rotate the Qibla arrow indicator on screen.
     * Angle 0 means top of phone points directly at Qibla.
     */
    fun calculateRelativeQiblaAngle(deviceHeading: Float, qiblaBearing: Float): Float {
        var diff = qiblaBearing - deviceHeading
        while (diff < -180f) diff += 360f
        while (diff >= 180f) diff -= 360f
        return diff
    }

    /**
     * Checks if current relative angle is aligned within a given tolerance (default ±3 degrees).
     */
    fun isAligned(relativeAngle: Float, toleranceDegrees: Float = 3.0f): Boolean {
        return abs(relativeAngle) <= toleranceDegrees
    }

    /**
     * Normalizes any angle in degrees to range [0, 360).
     */
    fun normalizeAngle(angle: Float): Float {
        var a = angle % 360f
        if (a < 0f) a += 360f
        return a
    }
}
