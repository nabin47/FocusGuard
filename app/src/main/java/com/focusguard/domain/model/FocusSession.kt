package com.focusguard.domain.model

/**
 * A focus session. [endedAt] is null while the session is still running.
 */
data class FocusSession(
    val id: Long = 0,
    val startedAt: Long,
    val endedAt: Long? = null,
    val tasksCompleted: Int = 0,
    val blockedAttempts: Int = 0,
    val completedAllTasks: Boolean = false
) {
    val isActive: Boolean get() = endedAt == null

    fun durationMillis(now: Long = System.currentTimeMillis()): Long =
        ((endedAt ?: now) - startedAt).coerceAtLeast(0)
}
