package com.focusguard.domain.repository

import com.focusguard.domain.model.BlockedApp
import com.focusguard.domain.model.Task
import kotlinx.coroutines.flow.StateFlow

data class FocusSessionState(
    val isFocusActive: Boolean = false,
    val activeTasks: List<Task> = emptyList(),
    val blockedApps: List<BlockedApp> = emptyList()
)

interface FocusSessionRepository {
    val sessionState: StateFlow<FocusSessionState>
    fun startFocusSession()
    fun stopFocusSession()
    fun updateActiveTasks(tasks: List<Task>)
    fun updateBlockedApps(apps: List<BlockedApp>)
}
