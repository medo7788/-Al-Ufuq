package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        QuranProgressEntity::class,
        AdhkarLogEntity::class,
        TasbeehRoutineEntity::class,
        ZakatRecordEntity::class,
        PrayerTrackEntity::class,
        PrayerTimeEntity::class,
        QuranSurahEntity::class,
        QuranAyahEntity::class,
        BookmarkEntity::class,
        AdhkarProgressEntity::class,
        UserSettingsEntity::class,
        UserGoalEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AlUfuqDatabase : RoomDatabase() {

    abstract fun alUfuqDao(): AlUfuqDao

    companion object {
        @Volatile
        private var INSTANCE: AlUfuqDatabase? = null

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `user_settings` (
                        `id` INTEGER NOT NULL PRIMARY KEY,
                        `cityName` TEXT NOT NULL,
                        `latitude` REAL NOT NULL,
                        `longitude` REAL NOT NULL,
                        `calculationMethod` INTEGER NOT NULL,
                        `asrSchool` INTEGER NOT NULL,
                        `muezzinSelection` TEXT NOT NULL,
                        `adhanEnabled` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `user_goals` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `title` TEXT NOT NULL,
                        `isCompleted` INTEGER NOT NULL,
                        `iconName` TEXT NOT NULL,
                        `dateStr` TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): AlUfuqDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AlUfuqDatabase::class.java,
                    "alufuq_database"
                )
                    .addMigrations(MIGRATION_4_5)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

