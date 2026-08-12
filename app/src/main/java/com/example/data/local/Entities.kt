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

@Entity(tableName = "adhkar_progress")
data class AdhkarProgressEntity(
    @PrimaryKey val itemId: Int,
    val category: String,
    val currentCount: Int,
    val dateStr: String
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

@Entity(tableName = "prayer_times")
data class PrayerTimeEntity(
    @PrimaryKey val dateStr: String, // "dd-MM-yyyy"
    val cityName: String,
    val hijriDateFormatted: String,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "quran_surahs")
data class QuranSurahEntity(
    @PrimaryKey val number: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val revelationType: String,
    val numberOfAyahs: Int,
    val pageNumber: Int
)

@Entity(tableName = "quran_ayahs")
data class QuranAyahEntity(
    @PrimaryKey val compositeId: String, // "${surahNumber}_${numberInSurah}"
    val surahNumber: Int,
    val numberInSurah: Int,
    val textArabic: String,
    val juz: Int,
    val page: Int,
    val sajda: Boolean
)

@Entity(tableName = "quran_bookmarks")
data class BookmarkEntity(
    @PrimaryKey val compositeId: String, // "${surahNumber}_${ayahNumber}"
    val surahNumber: Int,
    val surahName: String,
    val ayahNumber: Int,
    val ayahText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val cityName: String = "القاهرة، مصر",
    val latitude: Double = 30.0444,
    val longitude: Double = 31.2357,
    val calculationMethod: Int = 5,
    val asrSchool: Int = 0,
    val muezzinSelection: String = "مكة المكرمة",
    val adhanEnabled: Boolean = true
)

@Entity(tableName = "user_goals")
data class UserGoalEntity(
    @PrimaryKey val id: String,
    val title: String,
    val isCompleted: Boolean,
    val iconName: String,
    val dateStr: String
)

