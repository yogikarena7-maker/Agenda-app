package com.smartagenda.app.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.smartagenda.app.data.local.ReminderEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object NotificationScheduler {

    private const val TAG = "SmartAgendaNotif"
    const val CHANNEL_ID = "smart_agenda_reminders"
    private const val CHANNEL_NAME = "Smart Agenda Reminders"
    private const val CHANNEL_DESCRIPTION = "Notifications for scheduled agenda reminders and tasks"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                setShowBadge(true)
                setSound(soundUri, audioAttributes)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created: $CHANNEL_ID with bell notification sound")
        }
    }

    fun scheduleNotification(context: Context, reminder: ReminderEntity) {
        val reminderIdInt = reminder.id.toInt()

        // Verify if notifications are enabled on device level
        val notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        if (!notificationsEnabled) {
            Log.w(TAG, "Notifications are DISABLED for Smart Agenda in OS settings! Notification will not appear.")
        }

        // Do not schedule notifications for completed reminders
        if (reminder.isCompleted) {
            Log.d(TAG, "Reminder #${reminder.id} is completed. Cancelling notification.")
            cancelNotification(context, reminderIdInt)
            return
        }

        val triggerTimeMillis = parseTriggerTime(reminder.scheduledDate, reminder.scheduledTime)
        if (triggerTimeMillis == null) {
            Log.e(TAG, "FAILED to parse date '${reminder.scheduledDate}' or time '${reminder.scheduledTime}' for Reminder #${reminder.id}")
            return
        }

        val currentTime = System.currentTimeMillis()
        val diffSeconds = (triggerTimeMillis - currentTime) / 1000

        // Do not schedule if time is in the past
        if (triggerTimeMillis <= currentTime) {
            Log.w(TAG, "Scheduled time for Reminder #${reminder.id} ('${reminder.scheduledDate} ${reminder.scheduledTime}') is in the PAST (${diffSeconds}s ago). Skipping alarm.")
            return
        }

        Log.d(TAG, "Scheduling alarm for Reminder #${reminder.id} ('${reminder.title}') in ${diffSeconds}s (triggerMillis: $triggerTimeMillis)")

        val intent = Intent(context, ReminderNotificationReceiver::class.java).apply {
            putExtra(ReminderNotificationReceiver.EXTRA_REMINDER_ID, reminderIdInt)
            putExtra(ReminderNotificationReceiver.EXTRA_REMINDER_TITLE, reminder.title)
            putExtra(ReminderNotificationReceiver.EXTRA_REMINDER_NOTES, reminder.notes)
            putExtra(ReminderNotificationReceiver.EXTRA_REMINDER_DATE, reminder.scheduledDate)
            putExtra(ReminderNotificationReceiver.EXTRA_REMINDER_TIME, reminder.scheduledTime)
            putExtra(ReminderNotificationReceiver.EXTRA_REMINDER_CATEGORY, reminder.category)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderIdInt,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTimeMillis, pendingIntent)
                    alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                    Log.d(TAG, "Exact AlarmClock set successfully for Reminder #${reminder.id}")
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
                    Log.d(TAG, "Inexact setAndAllowWhileIdle set for Reminder #${reminder.id} (Exact alarms permission not granted)")
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTimeMillis, pendingIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                Log.d(TAG, "Exact AlarmClock set for Reminder #${reminder.id}")
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
                Log.d(TAG, "Exact Alarm set for Reminder #${reminder.id}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting alarm for Reminder #${reminder.id}", e)
        }
    }

    fun cancelNotification(context: Context, reminderId: Int) {
        try {
            val intent = Intent(context, ReminderNotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            pendingIntent?.let {
                alarmManager.cancel(it)
                it.cancel()
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(reminderId)
            Log.d(TAG, "Cancelled alarm & notification for Reminder #$reminderId")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling notification #$reminderId", e)
        }
    }

    fun parseTriggerTime(dateStr: String, timeStr: String): Long? {
        return try {
            val cleanDate = dateStr.trim()
            val cleanTime = timeStr.trim()

            val localDate = LocalDate.parse(cleanDate, DateTimeFormatter.ISO_LOCAL_DATE)
            val localTime = parseFlexibleTime(cleanTime) ?: return null

            val localDateTime = LocalDateTime.of(localDate, localTime)
            localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (e: Exception) {
            Log.e(TAG, "Exception parsing date '$dateStr' and time '$timeStr'", e)
            null
        }
    }

    private fun parseFlexibleTime(timeStr: String): LocalTime? {
        val formats = listOf(
            DateTimeFormatter.ofPattern("HH:mm"),
            DateTimeFormatter.ofPattern("H:mm"),
            DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("hh:mma", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("h:mma", Locale.ENGLISH)
        )

        for (formatter in formats) {
            try {
                return LocalTime.parse(timeStr, formatter)
            } catch (_: Exception) {
            }
        }
        return null
    }
}
