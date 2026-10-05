package com.focusguard.domain.model

import java.time.LocalDate

data class DailyFocus(
    val date: LocalDate,
    val focusMillis: Long
)

data class FocusStats(
    val todayFocusMillis: Long = 0,
    val todaySessions: Int = 0,
    val todayBlockedAttempts: Int = 0,
    val weekFocusMillis: Long = 0,
    val totalFocusMillis: Long = 0,
    val totalSessions: Int = 0,
    val currentStreakDays: Int = 0,
    /** Last 7 days, oldest first, ending today. */
    val last7Days: List<DailyFocus> = emptyList(),
    val recentSessions: List<FocusSession> = emptyList()
)
