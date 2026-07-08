package com.drasaty.app.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ScheduleEntry::class, TaskEntry::class, PomodoroSession::class],
    version = 1,
    exportSchema = false
)
abstract class DrasatyDatabase : RoomDatabase() {
    abstract fun scheduleDao(): ScheduleDao
    abstract fun taskDao(): TaskDao
    abstract fun pomodoroDao(): PomodoroDao

    companion object {
        @Volatile
        private var INSTANCE: DrasatyDatabase? = null

        fun getDatabase(context: Context): DrasatyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DrasatyDatabase::class.java,
                    "drasaty_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}