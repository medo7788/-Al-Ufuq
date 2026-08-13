package com.example.data.repository

import com.example.data.local.*
import com.example.data.models.*
import com.example.data.api.ApiClient
import com.example.data.datasource.AdhkarDataset
import com.example.utils.AdhkarValidator
import com.example.utils.QuranValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.text.SimpleDateFormat
import java.util.*

class AlUfuqRepository(private val dao: AlUfuqDao) {

    /** Exposes the DAO for one-time asset seeding (e.g. QuranSeeder). */
    fun dao(): AlUfuqDao = dao

    companion object {
        private const val BISMILLAH = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"

        /**
         * The Quran API embeds "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ" inside ayah 1's text
         * for every surah except At-Tawbah (9) — and the reading screen ALSO shows it as a
         * fixed header above the verse list, which duplicated it on screen. Al-Fatiha (1) is
         * the one case where the Bismillah genuinely IS the counted verse 1, so it must stay.
         */
        fun stripDuplicateBismillah(surahNumber: Int, verseNumber: Int, text: String): String {
            if (verseNumber != 1 || surahNumber == 1 || surahNumber == 9) return text
            val trimmed = text.trim()
            return if (trimmed.startsWith(BISMILLAH)) {
                trimmed.removePrefix(BISMILLAH).trim()
            } else {
                text
            }
        }
    }

    // --- Quran Data ---
    val quranProgress: Flow<QuranProgressEntity?> = dao.getQuranProgress()

    suspend fun updateQuranProgress(surahNumber: Int, surahName: String, verseNumber: Int, totalVerses: Int) {
        val current = QuranProgressEntity(
            id = 1,
            surahNumber = surahNumber,
            surahNameArabic = surahName,
            verseNumber = verseNumber,
            totalVersesInSurah = totalVerses,
            lastReadTimestamp = System.currentTimeMillis(),
            streakDays = 4
        )
        dao.saveQuranProgress(current)
    }

    // --- Quran Data & Caching ---
    suspend fun getSurahsList(): List<QuranSurah> {
        val cached = dao.getAllCachedSurahs()
        if (cached.size == 114) {
            val cachedMapped = cached.map {
                QuranSurah(
                    number = it.number,
                    nameArabic = it.nameArabic,
                    nameEnglish = it.nameEnglish,
                    totalVerses = it.numberOfAyahs,
                    revelationType = it.revelationType,
                    juzNumber = 1,
                    pageNumber = it.pageNumber
                )
            }
            if (QuranValidator.validateSurahCount(cachedMapped)) {
                return cachedMapped
            }
        }

        return try {
            val response = ApiClient.quranApi.getSurahsList()
            val dtos = response.body()?.data
            if (response.isSuccessful && !dtos.isNullOrEmpty()) {
                val apiSurahs = dtos.map { dto ->
                    QuranSurah(
                        number = dto.number,
                        nameArabic = dto.name,
                        nameEnglish = dto.englishName,
                        totalVerses = dto.numberOfAyahs,
                        revelationType = if (dto.revelationType.equals("Meccan", ignoreCase = true)) "مكية" else "مدنية",
                        juzNumber = 1,
                        pageNumber = 1
                    )
                }

                if (QuranValidator.validateSurahCount(apiSurahs)) {
                    val entities = apiSurahs.map { s ->
                        QuranSurahEntity(
                            number = s.number,
                            nameArabic = s.nameArabic,
                            nameEnglish = s.nameEnglish,
                            revelationType = s.revelationType,
                            numberOfAyahs = s.totalVerses,
                            pageNumber = s.pageNumber
                        )
                    }
                    dao.insertSurahs(entities)
                    apiSurahs
                } else if (cached.isNotEmpty()) {
                    cached.map {
                        QuranSurah(
                            number = it.number,
                            nameArabic = it.nameArabic,
                            nameEnglish = it.nameEnglish,
                            totalVerses = it.numberOfAyahs,
                            revelationType = it.revelationType,
                            juzNumber = 1,
                            pageNumber = it.pageNumber
                        )
                    }
                } else {
                    emptyList()
                }
            } else if (cached.isNotEmpty()) {
                cached.map {
                    QuranSurah(
                        number = it.number,
                        nameArabic = it.nameArabic,
                        nameEnglish = it.nameEnglish,
                        totalVerses = it.numberOfAyahs,
                        revelationType = it.revelationType,
                        juzNumber = 1,
                        pageNumber = it.pageNumber
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            if (cached.isNotEmpty()) {
                cached.map {
                    QuranSurah(
                        number = it.number,
                        nameArabic = it.nameArabic,
                        nameEnglish = it.nameEnglish,
                        totalVerses = it.numberOfAyahs,
                        revelationType = it.revelationType,
                        juzNumber = 1,
                        pageNumber = it.pageNumber
                    )
                }
            } else {
                emptyList()
            }
        }
    }

    suspend fun getVersesForSurah(surahNumber: Int): List<QuranVerse> {
        val cachedAyahs = dao.getAyahsForSurah(surahNumber)
        val expectedCount = QuranValidator.getExpectedAyahCount(surahNumber)
        if (cachedAyahs.size == expectedCount && expectedCount > 0) {
            val mapped = cachedAyahs.map {
                QuranVerse(
                    surahNumber = it.surahNumber,
                    verseNumber = it.numberInSurah,
                    textArabic = stripDuplicateBismillah(it.surahNumber, it.numberInSurah, it.textArabic),
                    translationArabic = "",
                    tafsirShort = "",
                    juz = it.juz,
                    page = it.page,
                    sajda = it.sajda
                )
            }
            val cachedSurah = dao.getCachedSurahByNumber(surahNumber)
            val surah = if (cachedSurah != null) {
                QuranSurah(
                    number = cachedSurah.number,
                    nameArabic = cachedSurah.nameArabic,
                    nameEnglish = cachedSurah.nameEnglish,
                    totalVerses = cachedSurah.numberOfAyahs,
                    revelationType = cachedSurah.revelationType,
                    juzNumber = 1,
                    pageNumber = cachedSurah.pageNumber,
                    ayahs = mapped
                )
            } else {
                QuranSurah(
                    number = surahNumber,
                    nameArabic = "سورة",
                    nameEnglish = "Surah",
                    totalVerses = expectedCount,
                    revelationType = "مكية",
                    juzNumber = 1,
                    pageNumber = 1,
                    ayahs = mapped
                )
            }
            if (QuranValidator.validateSurahDetail(surah)) {
                return mapped
            }
        }

        return try {
            val response = ApiClient.quranApi.getSurahDetailWithAudio(surahNumber)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                val apiVerses = data.ayahs.map {
                    QuranVerse(
                        surahNumber = surahNumber,
                        verseNumber = it.numberInSurah,
                        textArabic = stripDuplicateBismillah(surahNumber, it.numberInSurah, it.text),
                        translationArabic = "",
                        tafsirShort = "",
                        juz = it.juz,
                        page = it.page,
                        sajda = false
                    )
                }

                val surah = QuranSurah(
                    number = data.number,
                    nameArabic = data.name,
                    nameEnglish = data.englishName,
                    totalVerses = data.numberOfAyahs,
                    revelationType = "مكية",
                    juzNumber = 1,
                    pageNumber = 1,
                    ayahs = apiVerses
                )

                if (QuranValidator.validateSurahDetail(surah)) {
                    val entities = apiVerses.map { verse ->
                        QuranAyahEntity(
                            compositeId = "${surahNumber}_${verse.verseNumber}",
                            surahNumber = surahNumber,
                            numberInSurah = verse.verseNumber,
                            textArabic = verse.textArabic,
                            juz = verse.juz,
                            page = verse.page,
                            sajda = verse.sajda
                        )
                    }
                    dao.insertAyahs(entities)
                    apiVerses
                } else if (cachedAyahs.isNotEmpty()) {
                    cachedAyahs.map {
                        QuranVerse(
                            surahNumber = it.surahNumber,
                            verseNumber = it.numberInSurah,
                            textArabic = stripDuplicateBismillah(it.surahNumber, it.numberInSurah, it.textArabic),
                            translationArabic = "",
                            tafsirShort = "",
                            juz = it.juz,
                            page = it.page,
                            sajda = it.sajda
                        )
                    }
                } else {
                    emptyList()
                }
            } else if (cachedAyahs.isNotEmpty()) {
                cachedAyahs.map {
                    QuranVerse(
                        surahNumber = it.surahNumber,
                        verseNumber = it.numberInSurah,
                        textArabic = it.textArabic,
                        translationArabic = "",
                        tafsirShort = "",
                        juz = it.juz,
                        page = it.page,
                        sajda = it.sajda
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            if (cachedAyahs.isNotEmpty()) {
                cachedAyahs.map {
                    QuranVerse(
                        surahNumber = it.surahNumber,
                        verseNumber = it.numberInSurah,
                        textArabic = it.textArabic,
                        translationArabic = "",
                        tafsirShort = "",
                        juz = it.juz,
                        page = it.page,
                        sajda = it.sajda
                    )
                }
            } else {
                emptyList()
            }
        }
    }

    // --- Quran Bookmarks ---
    val bookmarks: Flow<List<BookmarkEntity>> = dao.getAllBookmarks()

    suspend fun addBookmark(surahNumber: Int, surahName: String, ayahNumber: Int, ayahText: String) {
        dao.addBookmark(
            BookmarkEntity(
                compositeId = "${surahNumber}_${ayahNumber}",
                surahNumber = surahNumber,
                surahName = surahName,
                ayahNumber = ayahNumber,
                ayahText = ayahText
            )
        )
    }

    suspend fun removeBookmark(surahNumber: Int, ayahNumber: Int) {
        dao.removeBookmark("${surahNumber}_${ayahNumber}")
    }

    // --- Adhkar Data ---
    fun getAdhkarCategories(): List<AdhkarCategory> {
        val categories = AdhkarDataset.CATEGORIES
        val items = AdhkarDataset.ALL_ITEMS
        if (AdhkarValidator.validateDataset(items) && AdhkarValidator.validateCategories(categories.map { it.titleArabic }, items)) {
            return categories
        }
        return emptyList()
    }

    suspend fun getAdhkarItems(category: String): List<AdhkarItem> {
        val baseItems = AdhkarDataset.getItemsForCategory(category)
        if (!AdhkarValidator.validateDataset(baseItems)) return emptyList()

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val progressList = dao.getAdhkarProgressForDate(todayStr)
        val progressMap = progressList.associateBy { it.itemId }

        return baseItems.map { item ->
            val savedCount = progressMap[item.id]?.currentCount ?: 0
            item.copy(currentCount = savedCount)
        }
    }

    suspend fun saveAdhkarProgress(itemId: Int, category: String, currentCount: Int) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        dao.saveAdhkarProgress(
            AdhkarProgressEntity(
                itemId = itemId,
                category = category,
                currentCount = currentCount,
                dateStr = todayStr
            )
        )
    }

    // --- Tasbeeh Routines ---
    val tasbeehRoutines: Flow<List<TasbeehRoutineEntity>> = dao.getTasbeehRoutines()

    suspend fun updateTasbeeh(id: Int, count: Int) {
        dao.updateTasbeehCount(id, count)
    }

    suspend fun saveRoutine(routine: TasbeehRoutineEntity) {
        dao.saveTasbeehRoutine(routine)
    }

    // --- Zakat Calculations ---
    val zakatRecords: Flow<List<ZakatRecordEntity>> = dao.getZakatRecords()

    suspend fun saveZakatRecord(cash: Double, goldGrams: Double, goldPrice: Double, debts: Double, totalZakat: Double) {
        dao.insertZakatRecord(
            ZakatRecordEntity(
                cashAmount = cash,
                gold24kGrams = goldGrams,
                goldPriceGram = goldPrice,
                debtsAmount = debts,
                totalZakatPayable = totalZakat
            )
        )
    }

    // --- Canonical Prayer Engine ---
    suspend fun getPrayerSchedule(
        city: String,
        country: String = "مصر",
        method: Int = 5,
        school: Int = 0
    ): PrayerSchedule {
        val todayStr = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date())
        return try {
            val response = com.example.data.api.ApiClient.aladhanApi.getTimingsByCity(
                city = city,
                country = country,
                method = method,
                school = school
            )
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                val schedule = PrayerSchedule.fromApiData(data, city, todayStr)
                // Cache into Room Database
                val entity = PrayerTimeEntity(
                    dateStr = schedule.dateStr.ifEmpty { todayStr },
                    cityName = schedule.cityName,
                    hijriDateFormatted = schedule.hijriDateFormatted,
                    fajr = schedule.fajr,
                    sunrise = schedule.sunrise,
                    dhuhr = schedule.dhuhr,
                    asr = schedule.asr,
                    maghrib = schedule.maghrib,
                    isha = schedule.isha
                )
                dao.savePrayerTimes(entity)
                schedule
            } else {
                getRoomOrFallbackSchedule(todayStr, city)
            }
        } catch (e: Exception) {
            getRoomOrFallbackSchedule(todayStr, city)
        }
    }

    suspend fun getPrayerScheduleByCoordinates(
        lat: Double,
        lng: Double,
        cityName: String,
        method: Int = 5,
        school: Int = 0
    ): PrayerSchedule {
        val todayStr = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date())
        return try {
            val response = com.example.data.api.ApiClient.aladhanApi.getTimingsByCoordinates(
                latitude = lat,
                longitude = lng,
                method = method,
                school = school
            )
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                val schedule = PrayerSchedule.fromApiData(data, cityName, todayStr)
                val entity = PrayerTimeEntity(
                    dateStr = schedule.dateStr.ifEmpty { todayStr },
                    cityName = schedule.cityName,
                    hijriDateFormatted = schedule.hijriDateFormatted,
                    fajr = schedule.fajr,
                    sunrise = schedule.sunrise,
                    dhuhr = schedule.dhuhr,
                    asr = schedule.asr,
                    maghrib = schedule.maghrib,
                    isha = schedule.isha
                )
                dao.savePrayerTimes(entity)
                schedule
            } else {
                getRoomOrFallbackSchedule(todayStr, cityName)
            }
        } catch (e: Exception) {
            getRoomOrFallbackSchedule(todayStr, cityName)
        }
    }

    private suspend fun getRoomOrFallbackSchedule(todayStr: String, city: String): PrayerSchedule {
        val cached = dao.getPrayerTimesForDate(todayStr) ?: dao.getLatestPrayerTimes()
        return if (cached != null) {
            PrayerSchedule.fromEntity(cached)
        } else {
            PrayerSchedule.fallback(cityName = city, dateStr = todayStr)
        }
    }

    // --- Your Moment Engine ---
    fun getYourMomentContext(prayerState: PrayerDayState, quranProgress: QuranProgressEntity?): YourMomentContext {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val nextName = prayerState.nextPrayer.nameArabic
        val remaining = prayerState.remainingTimeString

        return when {
            // Close to prayer (less than 30 mins remaining)
            prayerState.intervalProgressPercentage > 0.8f -> YourMomentContext(
                titleArabic = "اقتربت صلاة $nextName",
                subtitleArabic = "تبقى $remaining فقط • استعد وتوضأ للصلاة",
                actionLabelArabic = "عرض مواقيت الصلاة والقبلة",
                targetDestination = MomentTarget.PRAYER,
                badgeTagArabic = "تنبيه الصلاة"
            )
            // Morning time (5 AM to 9 AM)
            hour in 5..9 -> YourMomentContext(
                titleArabic = "بداية هادئة ليومك",
                subtitleArabic = "أنر صباحك بالذكر والقرآن الكريم",
                actionLabelArabic = "بدء أذكار الصباح (4 دقائق)",
                targetDestination = MomentTarget.MORNING_ADHKAR,
                badgeTagArabic = "أذكار الصباح"
            )
            // Afternoon/Evening (4 PM to 8 PM)
            hour in 16..20 -> YourMomentContext(
                titleArabic = "لحظة سكون وإقبال",
                subtitleArabic = "حصّن نفسك بأذكار المساء قبل غروب الشمس",
                actionLabelArabic = "بدء أذكار المساء (4 دقائق)",
                targetDestination = MomentTarget.EVENING_ADHKAR,
                badgeTagArabic = "أذكار المساء"
            )
            // Quran continuation
            else -> {
                val surah = quranProgress?.surahNameArabic ?: "سورة الكهف"
                val ayah = quranProgress?.verseNumber ?: 18
                YourMomentContext(
                    titleArabic = "تابع وردك القرآني",
                    subtitleArabic = "$surah • الآية $ayah • تبقّى حوالي 7 دقائق",
                    actionLabelArabic = "تابع القراءة من حيث توقفت",
                    targetDestination = MomentTarget.QURAN_CONTINUE,
                    badgeTagArabic = "وردك الحالي"
                )
            }
        }
    }

    // --- Persistent User Settings ---
    val userSettingsFlow: Flow<UserSettingsEntity?> = dao.getUserSettingsFlow()

    suspend fun getUserSettings(): UserSettingsEntity {
        return dao.getUserSettings() ?: UserSettingsEntity()
    }

    suspend fun saveUserSettings(settings: UserSettingsEntity) {
        dao.saveUserSettings(settings)
    }

    // --- Persistent User Goals ---
    fun getGoalsForDateFlow(dateStr: String): Flow<List<UserGoalEntity>> = dao.getGoalsForDateFlow(dateStr)

    suspend fun getGoalsForDate(dateStr: String): List<UserGoalEntity> {
        return dao.getGoalsForDate(dateStr)
    }

    suspend fun saveGoal(goal: UserGoalEntity) {
        dao.saveGoal(goal)
    }

    suspend fun saveGoals(goals: List<UserGoalEntity>) {
        dao.saveGoals(goals)
    }
}

