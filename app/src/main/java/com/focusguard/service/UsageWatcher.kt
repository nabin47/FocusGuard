package com.focusguard.service

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import javax.inject.Inject

/**
 * Tracks which app is in the foreground. Events are read incrementally so the
 * last known foreground app is remembered even when no new events arrive.
 */
class UsageWatcher @Inject constructor(
    private val usageStatsManager: UsageStatsManager
) {
    private var lastForegroundPackage: String? = null
    private var lastEventTime = 0L
    private var lastQueryEnd = 0L

    @Synchronized
    fun getForegroundApp(): String? {
        val now = System.currentTimeMillis()
        val from = if (lastQueryEnd == 0L) now - INITIAL_LOOKBACK_MS else lastQueryEnd - OVERLAP_MS
        val events = usageStatsManager.queryEvents(from, now)
        val event = UsageEvents.Event()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            @Suppress("DEPRECATION")
            val isForegroundEvent = event.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND
            if (isForegroundEvent && event.timeStamp >= lastEventTime) {
                lastEventTime = event.timeStamp
                lastForegroundPackage = event.packageName
            }
        }
        lastQueryEnd = now

        return lastForegroundPackage ?: usageStatsManager
            .queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - INITIAL_LOOKBACK_MS, now)
            ?.maxByOrNull { it.lastTimeUsed }
            ?.packageName
    }

    private companion object {
        const val INITIAL_LOOKBACK_MS = 60_000L
        const val OVERLAP_MS = 2_000L
    }
}
