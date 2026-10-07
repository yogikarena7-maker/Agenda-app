package com.smartagenda.app.notification

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.smartagenda.app.MainActivity
import com.smartagenda.app.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ReminderNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val reminderId = intent.getIntExtra(EXTRA_REMINDER_ID, 0)

        // Handle "Mark Complete" notification action button
        if (action == ACTION_MARK_COMPLETE) {
            Log.d(TAG, "Notification action ACTION_MARK_COMPLETE triggered for Reminder #$reminderId")
            if (reminderId != 0) {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = AppDatabase.getDatabase(context)
                        db.reminderDao().updateCompletionStatus(reminderId.toLong(), true)
                        NotificationScheduler.cancelNotification(context, reminderId)
                        Log.d(TAG, "Successfully marked Reminder #$reminderId complete from notification action")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error marking reminder #$reminderId complete from notification action", e)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
            return
        }

        val title = intent.getStringExtra(EXTRA_REMINDER_TITLE) ?: "Reminder"
        val notes = intent.getStringExtra(EXTRA_REMINDER_NOTES) ?: ""
        val scheduledDate = intent.getStringExtra(EXTRA_REMINDER_DATE) ?: ""
        val scheduledTime = intent.getStringExtra(EXTRA_REMINDER_TIME) ?: ""
        val category = intent.getStringExtra(EXTRA_REMINDER_CATEGORY) ?: "Personal"

        Log.d(TAG, "BroadcastReceiver ON_RECEIVE triggered for Reminder #$reminderId ('$title')")

        // Create notification tap action to open Smart Agenda MainActivity
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            reminderId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create "Mark Complete" notification action PendingIntent
        val completeIntent = Intent(context, ReminderNotificationReceiver::class.java).apply {
            setAction(ACTION_MARK_COMPLETE)
            putExtra(EXTRA_REMINDER_ID, reminderId)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId + 100000,
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Concise context formatting: e.g. "Today • 9:00 PM" or "Tomorrow • 9:00 AM"
        val timeContext = formatTimeContext(scheduledDate, scheduledTime)
        val bodyText = buildString {
            if (timeContext.isNotEmpty()) append("$timeContext ")
            if (category.isNotEmpty() && category != "Personal") append("• [$category] ")
            if (notes.isNotEmpty()) append("• $notes")
        }.trim()

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, NotificationScheduler.CHANNEL_ID)
            .setSmallIcon(com.smartagenda.app.R.drawable.ic_notification_logo)
            .setContentTitle(title)
            .setContentText(if (bodyText.isNotEmpty()) bodyText else "Smart Agenda Reminder")
            .setSubText("Smart Agenda")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setSound(soundUri)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(
                android.R.drawable.checkbox_on_background,
                "Mark Complete",
                completePendingIntent
            )

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            if (!notificationManager.areNotificationsEnabled()) {
                Log.e(TAG, "Cannot post notification: Notifications are DISABLED for Smart Agenda in system settings!")
                return
            }
            notificationManager.notify(reminderId, builder.build())
            Log.d(TAG, "System Notification posted successfully for Reminder #$reminderId ('$title')")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException posting notification for Reminder #$reminderId", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error posting notification for Reminder #$reminderId", e)
        }
    }

    private fun formatTimeContext(dateStr: String, timeStr: String): String {
        if (dateStr.isEmpty()) return timeStr
        return try {
            val targetDate = LocalDate.parse(dateStr.trim(), DateTimeFormatter.ISO_LOCAL_DATE)
            val today = LocalDate.now()
            val tomorrow = today.plusDays(1)

            val dayLabel = when (targetDate) {
                today -> "Today"
                tomorrow -> "Tomorrow"
                else -> targetDate.format(DateTimeFormatter.ofPattern("MMM d"))
            }

            if (timeStr.isNotEmpty()) "$dayLabel • $timeStr" else dayLabel
        } catch (_: Exception) {
            if (timeStr.isNotEmpty()) "$dateStr • $timeStr" else dateStr
        }
    }

    companion object {
        private const val TAG = "SmartAgendaNotif"
        const val ACTION_MARK_COMPLETE = "com.smartagenda.app.ACTION_MARK_COMPLETE"
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_REMINDER_TITLE = "extra_reminder_title"
        const val EXTRA_REMINDER_NOTES = "extra_reminder_notes"
        const val EXTRA_REMINDER_DATE = "extra_reminder_date"
        const val EXTRA_REMINDER_TIME = "extra_reminder_time"
        const val EXTRA_REMINDER_CATEGORY = "extra_reminder_category"
    }
}
