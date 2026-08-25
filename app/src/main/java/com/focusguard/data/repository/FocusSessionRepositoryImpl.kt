package com.focusguard.data.repository

import com.focusguard.domain.model.BlockedApp
import com.focusguard.domain.model.Task
import com.focusguard.domain.repository.FocusSessionRepository
import com.focusguard.domain.repository.FocusSessionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FocusSessionRepositoryImpl @Inject constructor() : FocusSessionRepository {

    private val _sessionState = MutableStateFlow(FocusSessionState())
    override val sessionState: StateFlow<FocusSessionState> = _sessionState.asStateFlow()

    override fun startFocusSession() {
        _sessionState.update { it.copy(isFocusActive = true) }
    }

    override fun stopFocusSession() {
        _sessionState.update { it.copy(isFocusActive = false) }
    }

    override fun updateActiveTasks(tasks: List<Task>) {
        _sessionState.update { currentState ->
            val updated = currentState.copy(activeTasks = tasks)
            // If focus is active and all tasks are completed, auto stop session
            if (updated.isFocusActive && tasks.isEmpty()) {
                updated.copy(isFocusActive = false)
            } else {
                updated
            }
        }
    }

    override fun updateBlockedApps(apps: List<BlockedApp>) {
        _sessionState.update { it.copy(blockedApps = apps) }
    }
}
