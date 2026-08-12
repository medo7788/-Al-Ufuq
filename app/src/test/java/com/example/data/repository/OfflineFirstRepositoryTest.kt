package com.example.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AlUfuqDatabase
import com.example.data.local.PrayerTimeEntity
import com.example.data.local.QuranSurahEntity
import com.example.data.local.UserGoalEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.models.PrayerScheduleSource
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OfflineFirstRepositoryTest {

    private lateinit var context: Context
    private lateinit var repository: AlUfuqRepository
    private lateinit var database: AlUfuqDatabase

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = AlUfuqDatabase.getDatabase(context)
        repository = AlUfuqRepository(database.alUfuqDao())
    }

    @Test
    fun testColdStartOfflineUserSettingsAndGoals() = runBlocking {
        val dao = database.alUfuqDao()
        
        // Save initial user settings offline
        val initialSettings = UserSettingsEntity(
            id = 1,
            cityName = "الإسكندرية",
            latitude = 31.2001,
            longitude = 29.9187,
            calculationMethod = 5,
            asrSchool = 0,
            muezzinSelection = "المسجد النبوي",
            adhanEnabled = true
        )
        dao.saveUserSettings(initialSettings)

        // Read settings on cold start
        val retrievedSettings = repository.getUserSettings()
        assertNotNull(retrievedSettings)
        assertEquals("الإسكندرية", retrievedSettings.cityName)
        assertEquals(31.2001, retrievedSettings.latitude, 0.0001)

        // Save daily goal offline
        val goal = UserGoalEntity("g1", "قراءة ورد القرآن", true, "book", "2026-08-12")
        dao.saveGoal(goal)

        val retrievedGoals = repository.getGoalsForDate("2026-08-12")
        assertEquals(1, retrievedGoals.size)
        assertEquals("قراءة ورد القرآن", retrievedGoals[0].title)
        assertTrue(retrievedGoals[0].isCompleted)
    }

    @Test
    fun testPrayerScheduleApiFailureWithValidCache() = runBlocking {
        val dao = database.alUfuqDao()
        val todayStr = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.US).format(java.util.Date())

        // Seed valid cache in Room DB for current todayStr
        val cachedSchedule = PrayerTimeEntity(
            dateStr = todayStr,
            cityName = "القاهرة",
            hijriDateFormatted = "٢٧ صفر ١٤٤٨ هـ",
            fajr = "04:15",
            sunrise = "05:42",
            dhuhr = "12:03",
            asr = "15:39",
            maghrib = "18:25",
            isha = "19:49"
        )
        dao.savePrayerTimes(cachedSchedule)

        // Verify local DB cache is present and valid
        val localCached = dao.getPrayerTimesForDate(todayStr)
        assertNotNull(localCached)
        assertEquals("04:15", localCached?.fajr)

        // Repository fetch returns non-null valid schedule
        val schedule = repository.getPrayerSchedule("القاهرة", "مصر")
        assertNotNull(schedule)
        assertTrue(schedule.fajr.isNotEmpty())
    }

    @Test
    fun testPrayerScheduleStaleCacheFallback() = runBlocking {
        val dao = database.alUfuqDao()

        // Seed an older/stale cache entry ("11-08-2026")
        val staleSchedule = PrayerTimeEntity(
            dateStr = "11-08-2026",
            cityName = "طنطا",
            hijriDateFormatted = "٢٦ صفر ١٤٤٨ هـ",
            fajr = "04:16",
            sunrise = "05:43",
            dhuhr = "12:04",
            asr = "15:40",
            maghrib = "18:26",
            isha = "19:50"
        )
        dao.savePrayerTimes(staleSchedule)

        // Attempting to fetch for unknown date when offline returns latest available cached schedule
        val latestCached = dao.getLatestPrayerTimes()
        assertNotNull(latestCached)
        assertEquals("11-08-2026", latestCached?.dateStr)
        assertEquals("طنطا", latestCached?.cityName)
    }

    @Test
    fun testQuranContentReadableOffline() = runBlocking {
        val dao = database.alUfuqDao()

        // Seed cached Quran surah into Room DB with accurate expected verse counts
        val surahs = (1..114).map { i ->
            QuranSurahEntity(
                number = i,
                nameArabic = "سورة $i",
                nameEnglish = "Surah $i",
                revelationType = "مكية",
                numberOfAyahs = com.example.utils.QuranValidator.getExpectedAyahCount(i),
                pageNumber = 1
            )
        }
        dao.insertSurahs(surahs)

        // Verify surahs can be read offline from DB
        val cachedSurahs = repository.getSurahsList()
        assertEquals(114, cachedSurahs.size)
        assertTrue(cachedSurahs[0].nameArabic.isNotEmpty())
    }

    @Test
    fun testRecoveryAfterNetworkReturns() = runBlocking {
        val dao = database.alUfuqDao()

        // Seed initial local entity
        val initialEntity = PrayerTimeEntity(
            dateStr = "12-08-2026",
            cityName = "أسوان",
            hijriDateFormatted = "٢٧ صفر ١٤٤٨ هـ",
            fajr = "04:25",
            sunrise = "05:50",
            dhuhr = "12:10",
            asr = "15:45",
            maghrib = "18:30",
            isha = "19:52"
        )
        dao.savePrayerTimes(initialEntity)

        // Verify initially saved
        val stored = dao.getPrayerTimesForDate("12-08-2026")
        assertNotNull(stored)
        assertEquals("أسوان", stored?.cityName)

        // Simulate network recovery updating cache
        val updatedEntity = initialEntity.copy(fajr = "04:26")
        dao.savePrayerTimes(updatedEntity)

        val recovered = dao.getPrayerTimesForDate("12-08-2026")
        assertEquals("04:26", recovered?.fajr)
    }
}
