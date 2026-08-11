package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        QuranProgressEntity::class,
        AdhkarLogEntity::class,
        TasbeehRoutineEntity::class,
        ZakatRecordEntity::class,
        PrayerTrackEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AlUfuqDatabase : RoomDatabase() {

    abstract fun alUfuqDao(): AlUfuqDao

    companion object {
        @Volatile
        private var INSTANCE: AlUfuqDatabase? = null

        fun getDatabase(context: Context): AlUfuqDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AlUfuqDatabase::class.java,
                    "alufuq_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
