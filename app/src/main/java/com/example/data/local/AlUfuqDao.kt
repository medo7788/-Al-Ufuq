package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AlUfuqDao {

    // Quran Progress
    @Query("SELECT * FROM quran_progress WHERE id = 1")
    fun getQuranProgress(): Flow<QuranProgressEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveQuranProgress(progress: QuranProgressEntity)

    // Adhkar Logs
    @Query("SELECT * FROM adhkar_logs ORDER BY timestamp DESC")
    fun getAllAdhkarLogs(): Flow<List<AdhkarLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdhkarLog(log: AdhkarLogEntity)

    @Query("SELECT * FROM adhkar_progress WHERE dateStr = :dateStr")
    suspend fun getAdhkarProgressForDate(dateStr: String): List<AdhkarProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAdhkarProgress(progress: AdhkarProgressEntity)

    // Tasbeeh Routines
    @Query("SELECT * FROM tasbeeh_routines")
    fun getTasbeehRoutines(): Flow<List<TasbeehRoutineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTasbeehRoutine(routine: TasbeehRoutineEntity)

    @Query("UPDATE tasbeeh_routines SET currentCount = :count WHERE id = :id")
    suspend fun updateTasbeehCount(id: Int, count: Int)

    // Zakat Records
    @Query("SELECT * FROM zakat_records ORDER BY timestamp DESC")
    fun getZakatRecords(): Flow<List<ZakatRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertZakatRecord(record: ZakatRecordEntity)

    // Prayer Tracking
    @Query("SELECT * FROM prayer_tracks WHERE dateIso = :dateIso")
    fun getPrayerTracksForDate(dateIso: String): Flow<List<PrayerTrackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePrayerTrack(track: PrayerTrackEntity)

    // Cached Prayer Schedule
    @Query("SELECT * FROM prayer_times WHERE dateStr = :dateStr")
    suspend fun getPrayerTimesForDate(dateStr: String): PrayerTimeEntity?

    @Query("SELECT * FROM prayer_times ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestPrayerTimes(): PrayerTimeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePrayerTimes(entity: PrayerTimeEntity)

    // Cached Quran Content
    @Query("SELECT * FROM quran_surahs ORDER BY number ASC")
    suspend fun getAllCachedSurahs(): List<QuranSurahEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurahs(surahs: List<QuranSurahEntity>)

    @Query("SELECT * FROM quran_surahs WHERE number = :number LIMIT 1")
    suspend fun getCachedSurahByNumber(number: Int): QuranSurahEntity?

    @Query("SELECT * FROM quran_ayahs WHERE surahNumber = :surahNumber ORDER BY numberInSurah ASC")
    suspend fun getAyahsForSurah(surahNumber: Int): List<QuranAyahEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAyahs(ayahs: List<QuranAyahEntity>)

    // Quran Bookmarks
    @Query("SELECT * FROM quran_bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM quran_bookmarks WHERE compositeId = :compositeId")
    suspend fun removeBookmark(compositeId: String)

    // User Settings
    @Query("SELECT * FROM user_settings WHERE id = 1")
    fun getUserSettingsFlow(): Flow<UserSettingsEntity?>

    @Query("SELECT * FROM user_settings WHERE id = 1")
    suspend fun getUserSettings(): UserSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserSettings(settings: UserSettingsEntity)

    // User Goals
    @Query("SELECT * FROM user_goals WHERE dateStr = :dateStr")
    fun getGoalsForDateFlow(dateStr: String): Flow<List<UserGoalEntity>>

    @Query("SELECT * FROM user_goals WHERE dateStr = :dateStr")
    suspend fun getGoalsForDate(dateStr: String): List<UserGoalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGoal(goal: UserGoalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGoals(goals: List<UserGoalEntity>)
}

