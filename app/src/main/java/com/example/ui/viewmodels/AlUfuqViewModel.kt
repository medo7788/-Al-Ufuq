package com.example.ui.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.ApiClient
import com.example.data.api.WeatherInfo
import com.example.data.local.*
import com.example.data.models.*
import com.example.data.repository.AlUfuqRepository
import com.example.utils.AudioPlayerManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

data class GoalItem(
    val id: String,
    val titleArabic: String,
    val isCompleted: Boolean = false,
    val iconName: String
)

data class MuezzinOption(
    val nameArabic: String,
    val description: String,
    val audioUrl: String
)

class AlUfuqViewModel(
    application: Application,
    private val repository: AlUfuqRepository
) : AndroidViewModel(application) {

    val audioPlayerManager = AudioPlayerManager(application)
    val audioState = audioPlayerManager.audioState

    // Weather State
    private val _weatherState = MutableStateFlow(WeatherInfo())
    val weatherState: StateFlow<WeatherInfo> = _weatherState.asStateFlow()

    // Location Detection State
    private val _isLocationLoading = MutableStateFlow(false)
    val isLocationLoading: StateFlow<Boolean> = _isLocationLoading.asStateFlow()

    private val _userCoordinates = MutableStateFlow<Pair<Double, Double>?>(Pair(30.0444, 31.2357))
    val userCoordinates: StateFlow<Pair<Double, Double>?> = _userCoordinates.asStateFlow()

    private val _locationStatusText = MutableStateFlow("القاهرة، مصر (موقع افتراضي)")
    val locationStatusText: StateFlow<String> = _locationStatusText.asStateFlow()

    // City & Location State
    private val _selectedCity = MutableStateFlow("القاهرة")
    val selectedCity: StateFlow<String> = _selectedCity.asStateFlow()

    private val _selectedCountry = MutableStateFlow("مصر")
    val selectedCountry: StateFlow<String> = _selectedCountry.asStateFlow()

    // Calculation Method Settings
    private val _calculationMethodCode = MutableStateFlow(5) // 5 = Egyptian Survey, 4 = Umm Al-Qura, etc.
    val calculationMethodCode: StateFlow<Int> = _calculationMethodCode.asStateFlow()

    private val _calculationMethodName = MutableStateFlow("الهيئة العامة المصرية للمساحة")
    val calculationMethodName: StateFlow<String> = _calculationMethodName.asStateFlow()

    // Asr Juristic School
    private val _asrSchoolCode = MutableStateFlow(0) // 0 = Standard, 1 = Hanafi
    val asrSchoolCode: StateFlow<Int> = _asrSchoolCode.asStateFlow()

    private val _asrSchoolName = MutableStateFlow("الجمهور (الشافعي / المالكي / الحنبلي)")
    val asrSchoolName: StateFlow<String> = _asrSchoolName.asStateFlow()

    // Muezzin & Audio Voices
    val muezzinOptions = listOf(
        MuezzinOption("أذان مكة المكرمة", "الشيخ علي ملا - الحرم المكي", "https://download.quranicaudio.com/adhan/makkah.mp3"),
        MuezzinOption("أذان المدينة المنورة", "الشيخ عصام بخاري - الحرم المدني", "https://www.islamcan.com/audio/adhan/azan2.mp3"),
        MuezzinOption("أذان المسجد الأقصى", "القدس الشريف", "https://www.islamcan.com/audio/adhan/azan3.mp3"),
        MuezzinOption("أذان الشيخ مشاري العفاسي", "صوت عذب خاشع", "https://www.islamcan.com/audio/adhan/azan4.mp3"),
        MuezzinOption("أذان الشيخ عبد الباسط عبد الصمد", "التلاوة الخالدة", "https://www.islamcan.com/audio/adhan/azan5.mp3")
    )

    private val _selectedMuezzin = MutableStateFlow(muezzinOptions[0])
    val selectedMuezzin: StateFlow<MuezzinOption> = _selectedMuezzin.asStateFlow()

    // Notification Mode
    private val _notificationMode = MutableStateFlow("صوت الأذان كامل")
    val notificationMode: StateFlow<String> = _notificationMode.asStateFlow()

    // Horizon Phase
    private val _horizonPhase = MutableStateFlow(HorizonPhase.getCurrentPhase(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)))
    val horizonPhase: StateFlow<HorizonPhase> = _horizonPhase.asStateFlow()

    // Prayer State
    private val _prayerState = MutableStateFlow(repository.calculateTodayPrayers())
    val prayerState: StateFlow<PrayerDayState> = _prayerState.asStateFlow()

    // Quran Progress
    val quranProgress: StateFlow<QuranProgressEntity?> = repository.quranProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), QuranProgressEntity())

    // Your Moment Context
    private val _yourMomentContext = MutableStateFlow(
        repository.getYourMomentContext(_prayerState.value, QuranProgressEntity())
    )
    val yourMomentContext: StateFlow<YourMomentContext> = _yourMomentContext.asStateFlow()

    // Quran Surahs
    val surahsList: List<QuranSurah> = repository.getSurahsList()

    private val _selectedSurahNumber = MutableStateFlow(1)
    val selectedSurahNumber: StateFlow<Int> = _selectedSurahNumber.asStateFlow()

    private val _readingVerses = MutableStateFlow(repository.getVersesForSurah(1))
    val readingVerses: StateFlow<List<QuranVerse>> = _readingVerses.asStateFlow()

    private val _isPlayingQuranAudio = MutableStateFlow(false)
    val isPlayingQuranAudio: StateFlow<Boolean> = _isPlayingQuranAudio.asStateFlow()

    // Adhkar State
    val adhkarCategories: List<AdhkarCategory> = repository.getAdhkarCategories()

    private val _activeAdhkarCategory = MutableStateFlow("أذكار الصباح")
    val activeAdhkarCategory: StateFlow<String> = _activeAdhkarCategory.asStateFlow()

    private val _activeAdhkarItems = MutableStateFlow(repository.getAdhkarItems("أذكار الصباح"))
    val activeAdhkarItems: StateFlow<List<AdhkarItem>> = _activeAdhkarItems.asStateFlow()

    // Tasbeeh State
    private val _tasbeehCount = MutableStateFlow(0)
    val tasbeehCount: StateFlow<Int> = _tasbeehCount.asStateFlow()

    private val _tasbeehTarget = MutableStateFlow(33)
    val tasbeehTarget: StateFlow<Int> = _tasbeehTarget.asStateFlow()

    private val _selectedDhikrTitle = MutableStateFlow("سُبْحَانَ اللّٰهِ وَبِحَمْدِهِ")
    val selectedDhikrTitle: StateFlow<String> = _selectedDhikrTitle.asStateFlow()

    private val _vibrationEnabled = MutableStateFlow(true)
    val vibrationEnabled: StateFlow<Boolean> = _vibrationEnabled.asStateFlow()

    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    // Zakat Records
    val zakatRecords: StateFlow<List<ZakatRecordEntity>> = repository.zakatRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Daily Goals
    private val _dailyGoals = MutableStateFlow(
        listOf(
            GoalItem("p5", "أداء الصلوات الخمس في وقتها", true, "mosque"),
            GoalItem("m_adhkar", "قراءة أذكار الصباح", true, "wb_sunny"),
            GoalItem("e_adhkar", "قراءة أذكار المساء", false, "nights_stay"),
            GoalItem("q_read", "قراءة ورد القرآن اليومي", true, "book"),
            GoalItem("tasbeeh", "التسبيح والذكر (33 مرة)", false, "fingerprint")
        )
    )
    val dailyGoals: StateFlow<List<GoalItem>> = _dailyGoals.asStateFlow()

    init {
        // Initial fetch for prayer times from Aladhan REST API and Weather from Open-Meteo
        fetchPrayerTimesFromApi()
        fetchWeather(30.0444, 31.2357)

        // Ticker loop
        viewModelScope.launch {
            while (true) {
                val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                _horizonPhase.value = HorizonPhase.getCurrentPhase(hour)

                val updatedPrayers = repository.calculateTodayPrayers()
                _prayerState.value = updatedPrayers

                _yourMomentContext.value = repository.getYourMomentContext(updatedPrayers, quranProgress.value)

                delay(1000)
            }
        }
    }

    fun fetchWeather(lat: Double, lng: Double) {
        viewModelScope.launch {
            try {
                val res = ApiClient.weatherApi.getCurrentWeather(lat, lng)
                if (res.isSuccessful && res.body()?.current_weather != null) {
                    _weatherState.value = WeatherInfo.fromDto(res.body()!!.current_weather)
                }
            } catch (e: Exception) {
                // Keep default or fallback
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun detectGPSLocationAndRefresh(context: Context) {
        _isLocationLoading.value = true
        viewModelScope.launch {
            try {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                var location: Location? = null
                
                if (locationManager != null) {
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    }
                    if (location == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    }
                }

                if (location != null) {
                    val lat = location.latitude
                    val lng = location.longitude
                    _userCoordinates.value = Pair(lat, lng)

                    // Get City Name via Geocoder if available
                    try {
                        val geocoder = Geocoder(context, Locale("ar"))
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(lat, lng, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val cityName = addresses[0].locality ?: addresses[0].subAdminArea ?: addresses[0].adminArea ?: "موقعي الحالي"
                            val countryName = addresses[0].countryName ?: "مصر"
                            _selectedCity.value = cityName
                            _selectedCountry.value = countryName
                            _locationStatusText.value = "$cityName، $countryName (موقع تلقائي 📍)"
                        } else {
                            _locationStatusText.value = "موقعي الحالي (📍 $lat, $lng)"
                        }
                    } catch (e: Exception) {
                        _locationStatusText.value = "موقعي الحالي (📍 $lat, $lng)"
                    }

                    fetchWeather(lat, lng)
                    fetchPrayerTimesByCoordinates(lat, lng)
                } else {
                    _locationStatusText.value = "تم تحديد موقع تقريبي بناءً على الإعدادات الحالية"
                    fetchWeather(_userCoordinates.value?.first ?: 30.0444, _userCoordinates.value?.second ?: 31.2357)
                    fetchPrayerTimesFromApi()
                }
            } catch (e: Exception) {
                _locationStatusText.value = "تعذر الحصول على إشارة GPS المباشرة"
                fetchPrayerTimesFromApi()
            } finally {
                _isLocationLoading.value = false
            }
        }
    }

    fun fetchPrayerTimesByCoordinates(lat: Double, lng: Double) {
        viewModelScope.launch {
            try {
                val response = ApiClient.aladhanApi.getTimingsByCoordinates(
                    latitude = lat,
                    longitude = lng,
                    method = _calculationMethodCode.value,
                    school = _asrSchoolCode.value
                )
                if (response.isSuccessful && response.body()?.data != null) {
                    val timings = response.body()!!.data!!.timings
                    val date = response.body()!!.data!!.date
                    
                    val updatedPrayers = repository.parseApiTimingsToState(
                        timings = timings,
                        hijriStr = "${date.hijri.day} ${date.hijri.month.ar ?: ""} ${date.hijri.year} هـ",
                        gregorianStr = "${date.gregorian.weekday?.en ?: ""} ${date.readable}"
                    )
                    _prayerState.value = updatedPrayers
                }
            } catch (e: Exception) {
                fetchPrayerTimesFromApi()
            }
        }
    }

    fun fetchPrayerTimesFromApi() {
        viewModelScope.launch {
            try {
                val response = ApiClient.aladhanApi.getTimingsByCity(
                    city = _selectedCity.value,
                    country = _selectedCountry.value,
                    method = _calculationMethodCode.value,
                    school = _asrSchoolCode.value
                )
                if (response.isSuccessful && response.body()?.data != null) {
                    val timings = response.body()!!.data!!.timings
                    val date = response.body()!!.data!!.date
                    
                    val updatedPrayers = repository.parseApiTimingsToState(
                        timings = timings,
                        hijriStr = "${date.hijri.day} ${date.hijri.month.ar ?: ""} ${date.hijri.year} هـ",
                        gregorianStr = "${date.gregorian.weekday?.en ?: ""} ${date.readable}"
                    )
                    _prayerState.value = updatedPrayers
                }
            } catch (e: Exception) {
                // Fallback to local offline calculation seamlessly
                _prayerState.value = repository.calculateTodayPrayers()
            }
        }
    }

    // Settings Update Actions
    fun setCity(cityName: String, countryName: String = "مصر") {
        _selectedCity.value = cityName
        _selectedCountry.value = countryName
        fetchPrayerTimesFromApi()
    }

    fun setCalculationMethod(code: Int, name: String) {
        _calculationMethodCode.value = code
        _calculationMethodName.value = name
        fetchPrayerTimesFromApi()
    }

    fun setAsrSchool(code: Int, name: String) {
        _asrSchoolCode.value = code
        _asrSchoolName.value = name
        fetchPrayerTimesFromApi()
    }

    fun selectMuezzin(option: MuezzinOption) {
        _selectedMuezzin.value = option
    }

    fun playAdhanPreview(audioUrl: String) {
        audioPlayerManager.playAudio(audioUrl, "معاينة صوت الأذان")
    }

    fun stopAudioPreview() {
        audioPlayerManager.stopAudio()
    }

    fun setNotificationMode(mode: String) {
        _notificationMode.value = mode
    }

    fun openSurah(surahNumber: Int) {
        _selectedSurahNumber.value = surahNumber
        _readingVerses.value = repository.getVersesForSurah(surahNumber)
        val surah = surahsList.find { it.number == surahNumber }
        if (surah != null) {
            viewModelScope.launch {
                repository.updateQuranProgress(surah.number, surah.nameArabic, 1, surah.totalVerses)
            }
        }

        // Fetch real Ayahs from Quran Cloud API asynchronously
        viewModelScope.launch {
            try {
                val res = ApiClient.quranApi.getSurahDetailWithAudio(surahNumber)
                if (res.isSuccessful && res.body()?.data != null) {
                    val ayahsDto = res.body()!!.data!!.ayahs
                    val apiVerses = ayahsDto.map {
                        QuranVerse(
                            surahNumber = surahNumber,
                            verseNumber = it.numberInSurah,
                            textArabic = it.text,
                            translationArabic = "آية رقم ${it.numberInSurah} من سورة ${surah?.nameArabic}"
                        )
                    }
                    _readingVerses.value = apiVerses
                }
            } catch (e: Exception) {
                // Keep local fallback
            }
        }
    }

    fun playSurahAudio(surahNumber: Int) {
        val audioUrl = "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/$surahNumber.mp3"
        audioPlayerManager.playAudio(audioUrl, "تلاوة سورة رقم $surahNumber")
    }

    fun selectAdhkarCategory(categoryTitle: String) {
        _activeAdhkarCategory.value = categoryTitle
        _activeAdhkarItems.value = repository.getAdhkarItems(categoryTitle)
    }

    fun incrementAdhkarItem(itemId: Int) {
        _activeAdhkarItems.value = _activeAdhkarItems.value.map { item ->
            if (item.id == itemId && item.currentCount < item.targetCount) {
                item.copy(currentCount = item.currentCount + 1)
            } else {
                item
            }
        }
    }

    fun incrementTasbeeh() {
        if (_tasbeehCount.value < _tasbeehTarget.value - 1) {
            _tasbeehCount.value += 1
        } else {
            _tasbeehCount.value = _tasbeehTarget.value
            // Auto reset after target
            viewModelScope.launch {
                delay(300)
                _tasbeehCount.value = 0
            }
        }
    }

    fun resetTasbeeh() {
        _tasbeehCount.value = 0
    }

    fun setTasbeehPreset(title: String, target: Int) {
        _selectedDhikrTitle.value = title
        _tasbeehTarget.value = target
        _tasbeehCount.value = 0
    }

    fun toggleVibration() {
        _vibrationEnabled.value = !_vibrationEnabled.value
    }

    fun toggleSound() {
        _soundEnabled.value = !_soundEnabled.value
    }

    fun toggleGoal(goalId: String) {
        _dailyGoals.value = _dailyGoals.value.map { goal ->
            if (goal.id == goalId) goal.copy(isCompleted = !goal.isCompleted) else goal
        }
    }

    fun saveZakatCalculation(cash: Double, gold24k: Double, goldPrice: Double, debts: Double, totalZakat: Double) {
        viewModelScope.launch {
            repository.saveZakatRecord(cash, gold24k, goldPrice, debts, totalZakat)
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayerManager.stopAudio()
    }
}
