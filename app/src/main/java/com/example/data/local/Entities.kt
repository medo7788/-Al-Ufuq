package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quran_progress")
data class QuranProgressEntity(
    @PrimaryKey val id: Int = 1,
    val surahNumber: Int = 18,
    val surahNameArabic: String = "سورة الكهف",
    val verseNumber: Int = 18,
    val totalVersesInSurah: Int = 110,
    val lastReadTimestamp: Long = System.currentTimeMillis(),
    val streakDays: Int = 3
)

@Entity(tableName = "adhkar_logs")
data class AdhkarLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,
    val completedCount: Int,
    val totalCount: Int,
    val isCompleted: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasbeeh_routines")
data class TasbeehRoutineEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val currentCount: Int,
    val targetCount: Int,
    val vibrationEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val isFavorite: Boolean = true
)

@Entity(tableName = "zakat_records")
data class ZakatRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val cashAmount: Double,
    val gold24kGrams: Double,
    val goldPriceGram: Double,
    val debtsAmount: Double,
    val totalZakatPayable: Double
)

@Entity(tableName = "prayer_tracks")
data class PrayerTrackEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val prayerName: String,
    val dateIso: String,
    val isCompleted: Boolean,
    val onTime: Boolean
)
