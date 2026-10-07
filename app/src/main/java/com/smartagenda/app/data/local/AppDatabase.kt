package com.smartagenda.app.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [ReminderEntity::class, EmailMessageEntity::class, EmailDraftEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun reminderDao(): ReminderDao
    abstract fun emailDao(): EmailDao

    companion object {
        private const val TAG = "AppDatabase"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_agenda_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(context.applicationContext))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            populateDatabase(database.reminderDao())
                        } catch (e: Exception) {
                            Log.e(TAG, "Error populating database callback", e)
                        }
                    }
                }
            }

            suspend fun populateDatabase(dao: ReminderDao) {
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

                val sampleReminders = listOf(
                    ReminderEntity(
                        title = "Design System Review",
                        notes = "Review Kinetic Obsidian UI components with the design team.",
                        scheduledDate = todayStr,
                        scheduledTime = "08:30",
                        category = "Work",
                        isCompleted = true
                    ),
                    ReminderEntity(
                        title = "Smart Agenda Sprint Planning",
                        notes = "Discuss Phase 1 Room database integration and dynamic timeline state.",
                        scheduledDate = todayStr,
                        scheduledTime = "10:00",
                        category = "Work",
                        isCompleted = false
                    ),
                    ReminderEntity(
                        title = "Lunch & Team Sync",
                        notes = "Catch up on project roadmap over lunch.",
                        scheduledDate = todayStr,
                        scheduledTime = "12:15",
                        category = "Personal",
                        isCompleted = false
                    ),
                    ReminderEntity(
                        title = "Evening Workout & Hydration",
                        notes = "Light cardio session and daily wellness check.",
                        scheduledDate = todayStr,
                        scheduledTime = "17:30",
                        category = "Urgent",
                        isCompleted = false
                    )
                )

                sampleReminders.forEach { dao.insertReminder(it) }
            }
        }
    }
}
