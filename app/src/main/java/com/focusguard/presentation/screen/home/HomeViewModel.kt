package com.focusguard.presentation.screen.home

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusguard.domain.model.Task
import com.focusguard.domain.repository.FocusSessionRepository
import com.focusguard.domain.repository.FocusSessionState
import com.focusguard.domain.usecase.AddTaskUseCase
import com.focusguard.domain.usecase.CompleteTaskUseCase
import com.focusguard.domain.usecase.GetActiveTasksUseCase
import com.focusguard.domain.usecase.GetBlockedAppsUseCase
import com.focusguard.service.FocusMonitorService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getActiveTasksUseCase: GetActiveTasksUseCase,
    private val addTaskUseCase: AddTaskUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val getBlockedAppsUseCase: GetBlockedAppsUseCase,
    private val focusSessionRepository: FocusSessionRepository
) : ViewModel() {

    val tasks: StateFlow<List<Task>> = getActiveTasksUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sessionState: StateFlow<FocusSessionState> = focusSessionRepository.sessionState

    init {
        // Observe active tasks and blocked apps to keep session state in sync
        viewModelScope.launch {
            getActiveTasksUseCase().collect { activeTasks ->
                focusSessionRepository.updateActiveTasks(activeTasks)
            }
        }
        viewModelScope.launch {
            getBlockedAppsUseCase().collect { blockedApps ->
                focusSessionRepository.updateBlockedApps(blockedApps)
            }
        }
    }

    fun addTask(title: String) {
        viewModelScope.launch {
            addTaskUseCase(title)
        }
    }

    fun completeTask(taskId: Long) {
        viewModelScope.launch {
            completeTaskUseCase(taskId)
        }
    }

    fun toggleFocusSession(context: Context) {
        val currentState = sessionState.value.isFocusActive
        if (currentState) {
            focusSessionRepository.stopFocusSession()
            val intent = Intent(context, FocusMonitorService::class.java).apply {
                action = FocusMonitorService.ACTION_STOP
            }
            context.startService(intent)
        } else {
            focusSessionRepository.startFocusSession()
            val intent = Intent(context, FocusMonitorService::class.java).apply {
                action = FocusMonitorService.ACTION_START
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
