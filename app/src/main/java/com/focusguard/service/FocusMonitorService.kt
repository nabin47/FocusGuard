package com.focusguard.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.focusguard.MainActivity
import com.focusguard.R
import com.focusguard.domain.repository.FocusSessionRepository
import com.focusguard.presentation.screen.blocked.BlockedOverlayActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FocusMonitorService : Service() {

    @Inject
    lateinit var usageWatcher: UsageWatcher

    @Inject
    lateinit var focusSessionRepository: FocusSessionRepository

    private val monitorScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var isMonitoring = false

    companion object {
        private const val CHANNEL_ID = "focus_guard_monitor_channel"
        private const val NOTIFICATION_ID = 1001
        const val ACTION_START = "ACTION_START_FOCUS"
        const val ACTION_STOP = "ACTION_STOP_FOCUS"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startForegroundWithNotification()
                if (!isMonitoring) {
                    startMonitoring()
                }
            }
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            else -> {
                startForegroundWithNotification()
                if (!isMonitoring) {
                    startMonitoring()
                }
            }
        }
        return START_STICKY
    }

    private fun startForegroundWithNotification() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun startMonitoring() {
        isMonitoring = true

        // Observe session state to auto stop service when focus is no longer active
        monitorScope.launch {
            focusSessionRepository.sessionState.collect { sessionState ->
                if (!sessionState.isFocusActive) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }

        // Polling loop checking current dynamic session state
        monitorScope.launch {
            while (isActive) {
                val currentState = focusSessionRepository.sessionState.value
                if (currentState.isFocusActive) {
                    val foregroundApp = usageWatcher.getForegroundApp()
                    if (foregroundApp != null && foregroundApp != packageName) {
                        val isBlocked = currentState.blockedApps.any { it.packageName == foregroundApp }
                        if (isBlocked) {
                            showBlockOverlay()
                        }
                    }
                }
                delay(1000L)
            }
        }
    }

    private fun showBlockOverlay() {
        val intent = Intent(this, BlockedOverlayActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        isMonitoring = false
        monitorScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
