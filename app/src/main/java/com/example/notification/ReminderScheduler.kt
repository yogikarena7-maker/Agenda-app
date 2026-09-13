package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.local.ReminderEntity

object ReminderScheduler {
    private const val TAG = "ReminderScheduler"

    fun schedule(context: Context, reminder: ReminderEntity) {
        // Do not schedule alarms for completed reminders or reminders in the past
        val now = System.currentTimeMillis()
        if (reminder.isCompleted || reminder.scheduledEpochMillis <= now) {
            cancel(context, reminder.id)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // First cancel any existing alarm with this reminder ID
        cancel(context, reminder.id)

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_REMINDER_ID, reminder.id)
            putExtra(ReminderReceiver.EXTRA_TITLE, reminder.title)
            putExtra(ReminderReceiver.EXTRA_NOTES, reminder.notes)
            putExtra(ReminderReceiver.EXTRA_TIME, reminder.timeString)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminder.scheduledEpochMillis,
                        pendingIntent
                    )
                } else {
                    // Fallback to non-exact but still while-idle alarm
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminder.scheduledEpochMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminder.scheduledEpochMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled reminder ${reminder.id} for ${reminder.scheduledEpochMillis}")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException scheduling reminder exact alarm, falling back", e)
            try {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminder.scheduledEpochMillis,
                    pendingIntent
                )
            } catch (fallbackEx: Exception) {
                Log.e(TAG, "Failed to schedule reminder alarm fallback", fallbackEx)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling reminder alarm", e)
        }
    }

    fun cancel(context: Context, reminderId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Cancelled reminder alarm $reminderId")
        }
    }
}
