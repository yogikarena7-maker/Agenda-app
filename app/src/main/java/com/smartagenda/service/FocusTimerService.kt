package com.smartagenda.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FocusTimerService : Service() {

    enum class TimerAction { START, PAUSE, STOP, ADD_5_MIN }

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private lateinit var notificationManager: NotificationManager

    private var isPaused = false
    private var baseTime = 0L
    private var timeRemainingMs = 0L
    private val defaultSessionLengthMs = 25 * 60 * 1000L // 25 Minutes

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY

        when (action) {
            TimerAction.START.name -> startFocusSession()
            TimerAction.PAUSE.name -> pauseFocusSession()
            TimerAction.STOP.name -> stopFocusSession()
            TimerAction.ADD_5_MIN.name -> addFiveMinutes()
        }
        return START_STICKY
    }

    private fun startFocusSession() {
        if (baseTime == 0L) {
            timeRemainingMs = defaultSessionLengthMs
        }
        isPaused = false
        baseTime = SystemClock.elapsedRealtime() + timeRemainingMs

        startForegroundServiceWithNotification()

        serviceScope.launch {
            while (!isPaused && timeRemainingMs > 0) {
                timeRemainingMs = baseTime - SystemClock.elapsedRealtime()
                _timerState.update { 
                    it.copy(
                        isRunning = true,
                        remainingMs = timeRemainingMs.coerceAtLeast(0L),
                        progress = timeRemainingMs.toFloat() / defaultSessionLengthMs 
                    ) 
                }
                updateNotification()
                delay(100L)
            }
            if (timeRemainingMs <= 0) {
                stopFocusSession()
            }
        }
    }

    private fun pauseFocusSession() {
        isPaused = true
        _timerState.update { it.copy(isRunning = false) }
        updateNotification()
    }

    private fun stopFocusSession() {
        isPaused = true
        baseTime = 0L
        timeRemainingMs = 0L
        _timerState.update { it.copy(isRunning = false, remainingMs = 0L, progress = 0f) }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun addFiveMinutes() {
        val addedMs = 5 * 60 * 1000L
        timeRemainingMs += addedMs
        baseTime += addedMs
        _timerState.update { it.copy(remainingMs = timeRemainingMs) }
        updateNotification()
    }

    private fun startForegroundServiceWithNotification() {
        val notification = buildOngoingNotification()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                } else {
                    0 
                }
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification() {
        notificationManager.notify(NOTIFICATION_ID, buildOngoingNotification())
    }

    private fun buildOngoingNotification(): Notification {
        val pauseIntent = Intent(this, FocusTimerService::class.java).apply { action = TimerAction.PAUSE.name }
        val pausePendingIntent = PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val addTimeIntent = Intent(this, FocusTimerService::class.java).apply { action = TimerAction.ADD_5_MIN.name }
        val addTimePendingIntent = PendingIntent.getService(this, 2, addTimeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val stopIntent = Intent(this, FocusTimerService::class.java).apply { action = TimerAction.STOP.name }
        val stopPendingIntent = PendingIntent.getService(this, 3, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Zenda Focus Session Active")
            .setContentText("Focusing on the task...")
            .setUsesChronometer(true)
            .setWhen(baseTime - SystemClock.elapsedRealtime() + System.currentTimeMillis())
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(android.R.drawable.ic_media_pause, "Pause", pausePendingIntent)
            .addAction(android.R.drawable.ic_menu_add, "+5m", addTimePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Focus Timer Ongoing",
                NotificationManager.IMPORTANCE_LOW 
            ).apply {
                description = "Ongoing notification for active Zenda focus sessions."
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "zenda_focus_channel"
        private const val NOTIFICATION_ID = 8848

        private val _timerState = MutableStateFlow(TimerState())
        val timerState: StateFlow<TimerState> = _timerState.asStateFlow()
    }
}

data class TimerState(
    val isRunning: Boolean = false,
    val remainingMs: Long = 0L,
    val progress: Float = 0f
)
