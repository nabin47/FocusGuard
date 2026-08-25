package com.focusguard.service

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import javax.inject.Inject

class UsageWatcher @Inject constructor(
    private val usageStatsManager: UsageStatsManager
) {
    fun getForegroundApp(): String? {
        val now = System.currentTimeMillis()
        val events = usageStatsManager.queryEvents(now - 10000, now)
        val event = UsageEvents.Event()
        var currentForegroundPackage: String? = null
        var maxTime: Long = 0

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND
            ) {
                if (event.timeStamp > maxTime) {
                    maxTime = event.timeStamp
                    currentForegroundPackage = event.packageName
                }
            }
        }

        if (currentForegroundPackage != null) {
            return currentForegroundPackage
        }

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, now - 10000, now
        )
        return stats?.maxByOrNull { it.lastTimeUsed }?.packageName
    }
}
