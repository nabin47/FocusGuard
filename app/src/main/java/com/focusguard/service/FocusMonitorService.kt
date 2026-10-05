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
import androidx.core.content.ContextCompat
import com.focusguard.MainActivity
import com.focusguard.R
import com.focusguard.domain.repository.FocusSessionRepository
import com.focusguard.domain.repository.FocusSessionState
import com.focusguard.presentation.screen.blocked.BlockedOverlayActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
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
    private var monitorJob: Job? = null

    companion object {
        private const val CHANNEL_ID = "focus_guard_monitor_channel"
        private const val NOTIFICATION_ID = 1001
        private const val POLL_INTERVAL_MS = 1000L
        const val ACTION_START = "ACTION_START_FOCUS"
        const val ACTION_STOP = "ACTION_STOP_FOCUS"

        fun start(context: Context) {
            val intent = Intent(context, FocusMonitorService::class.java).setAction(ACTION_START)
            try {
                ContextCompat.startForegroundService(context, intent)
            } catch (e: IllegalStateException) {
                // Not allowed from the background; the next call from the UI will start it.
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, FocusMonitorService::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopMonitoringService()
            return START_NOT_STICKY
        }
        // ACTION_START, or a sticky restart after the process was killed.
        try {
            startForeground(NOTIFICATION_ID, buildNotification(focusSessionRepository.sessionState.value))
        } catch (e: Exception) {
            // Starting in the foreground can be refused while the app is in the background.
            stopSelf()
            return START_NOT_STICKY
        }
        startMonitoring()
        return START_STICKY
    }

    private fun startMonitoring() {
        if (monitorJob?.isActive == true) return

        monitorJob = monitorScope.launch {
            // Stop once the session ends; keep the notification in sync while it runs.
            launch {
                focusSessionRepository.sessionState
                    .filter { it.isLoaded }
                    .distinctUntilChangedBy { Triple(it.isFocusActive, it.startedAt, it.activeTasks.size) }
                    .collect { state ->
                        if (state.isFocusActive) updateNotification(state) else stopMonitoringService()
                    }
            }

            // Poll the foreground app and cover blocked apps with the overlay.
            launch {
                var lastForeground: String? = null
                while (isActive) {
                    val state = focusSessionRepository.sessionState.value
                    if (state.isFocusActive) {
                        val foregroundApp = usageWatcher.getForegroundApp()
                        if (foregroundApp != null && foregroundApp != packageName && state.isBlocked(foregroundApp)) {
                            // Count each time the user switches into a blocked app, not every poll.
                            if (foregroundApp != lastForeground) {
                                focusSessionRepository.recordBlockedAttempt()
                            }
                            showBlockOverlay(foregroundApp)
                        }
                        lastForeground = foregroundApp
                    }
                    delay(POLL_INTERVAL_MS)
                }
            }
        }
    }

    private fun stopMonitoringService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun showBlockOverlay(blockedPackage: String) {
        val intent = Intent(this, BlockedOverlayActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(BlockedOverlayActivity.EXTRA_BLOCKED_PACKAGE, blockedPackage)
        }
        startActivity(intent)
    }

    private fun buildNotification(state: FocusSessionState): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val remaining = state.activeTasks.size
        val text = if (state.isLoaded && remaining > 0) {
            resources.getQuantityString(R.plurals.notification_tasks_left, remaining, remaining)
        } else {
            getString(R.string.notification_text)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_stat_focus)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .apply {
                // A live stopwatch showing how long the session has been running.
                state.startedAt?.let { setWhen(it).setShowWhen(true).setUsesChronometer(true) }
            }
            .build()
    }

    private fun updateNotification(state: FocusSessionState) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification(state))
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
        monitorScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
