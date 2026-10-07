package com.smartagenda.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.smartagenda.app.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "BootReceiver received action: $action")

        if (Intent.ACTION_BOOT_COMPLETED == action || Intent.ACTION_MY_PACKAGE_REPLACED == action) {
            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Ensure Notification Channel is registered
                    NotificationScheduler.createNotificationChannel(context)

                    val database = AppDatabase.getDatabase(context)
                    val reminderDao = database.reminderDao()
                    val allReminders = reminderDao.getAllRemindersList()

                    val currentTime = System.currentTimeMillis()
                    var rescheduledCount = 0

                    for (reminder in allReminders) {
                        // Skip completed reminders
                        if (reminder.isCompleted) {
                            continue
                        }

                        val triggerTime = NotificationScheduler.parseTriggerTime(
                            reminder.scheduledDate,
                            reminder.scheduledTime
                        )

                        // Only reschedule future uncompleted reminders
                        if (triggerTime != null && triggerTime > currentTime) {
                            NotificationScheduler.scheduleNotification(context, reminder)
                            rescheduledCount++
                            Log.d(TAG, "Re-scheduled Reminder #${reminder.id} ('${reminder.title}') after reboot.")
                        } else {
                            Log.d(TAG, "Skipping past/invalid Reminder #${reminder.id} after reboot.")
                        }
                    }

                    Log.d(TAG, "BootReceiver completed: Rescheduled $rescheduledCount future uncompleted reminders.")
                } catch (e: Exception) {
                    Log.e(TAG, "Error in BootReceiver during reminder rescheduling", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    companion object {
        private const val TAG = "SmartAgendaBoot"
    }
}
