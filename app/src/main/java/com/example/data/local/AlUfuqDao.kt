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
}
