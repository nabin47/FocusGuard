package com.focusguard.domain.repository

import com.focusguard.domain.model.BlockedApp
import com.focusguard.domain.model.FocusSession
import com.focusguard.domain.model.Task
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

data class FocusSessionState(
    /** False until the persisted session, tasks and blocked apps have been loaded. */
    val isLoaded: Boolean = false,
    val isFocusActive: Boolean = false,
    val startedAt: Long? = null,
    val blockedAttempts: Int = 0,
    val activeTasks: List<Task> = emptyList(),
    val blockedApps: List<BlockedApp> = emptyList()
) {
    fun isBlocked(packageName: String): Boolean =
        isFocusActive && blockedApps.any { it.packageName == packageName }
}

/**
 * Single source of truth for the live focus session. It keeps tasks and blocked
 * apps in sync on its own, persists every session for time tracking and ends the
 * session automatically once every task is complete.
 */
interface FocusSessionRepository {
    val sessionState: StateFlow<FocusSessionState>

    /** Emits each session right after it ends. */
    val endedSessions: SharedFlow<FocusSession>

    /** Starts a session. Returns false if there is nothing to focus on. */
    fun startFocusSession(): Boolean
    fun stopFocusSession()
    fun recordBlockedAttempt()
}
