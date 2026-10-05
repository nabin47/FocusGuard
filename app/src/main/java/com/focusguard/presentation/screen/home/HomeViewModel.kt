package com.focusguard.presentation.screen.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusguard.domain.model.FocusSession
import com.focusguard.domain.model.FocusStats
import com.focusguard.domain.model.Task
import com.focusguard.domain.repository.FocusSessionRepository
import com.focusguard.domain.repository.FocusSessionState
import com.focusguard.domain.repository.TaskRepository
import com.focusguard.domain.usecase.AddTaskUseCase
import com.focusguard.domain.usecase.GetFocusStatsUseCase
import com.focusguard.domain.util.TimeProvider
import com.focusguard.service.FocusMonitorService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

sealed interface HomeEvent {
    data class TaskCompleted(val task: Task) : HomeEvent
    data class TaskDeleted(val task: Task) : HomeEvent
    data class SessionEnded(val session: FocusSession) : HomeEvent
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val taskRepository: TaskRepository,
    private val addTaskUseCase: AddTaskUseCase,
    private val focusSessionRepository: FocusSessionRepository,
    getFocusStatsUseCase: GetFocusStatsUseCase,
    timeProvider: TimeProvider
) : ViewModel() {

    val sessionState: StateFlow<FocusSessionState> = focusSessionRepository.sessionState

    val tasks: StateFlow<List<Task>> = sessionState
        .map { it.activeTasks }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), sessionState.value.activeTasks)

    private val startOfToday: Long = Instant.ofEpochMilli(timeProvider.now())
        .atZone(timeProvider.zone())
        .toLocalDate()
        .atStartOfDay(timeProvider.zone())
        .toInstant()
        .toEpochMilli()

    val completedToday: StateFlow<List<Task>> = taskRepository.getTasksCompletedSince(startOfToday)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stats: StateFlow<FocusStats> = getFocusStatsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FocusStats())

    private val _events = MutableSharedFlow<HomeEvent>(extraBufferCapacity = 8)
    val events: Flow<HomeEvent> = merge(
        _events,
        focusSessionRepository.endedSessions.map { HomeEvent.SessionEnded(it) }
    )

    fun addTask(title: String) {
        viewModelScope.launch { addTaskUseCase(title) }
    }

    fun completeTask(task: Task) {
        viewModelScope.launch {
            taskRepository.completeTask(task.id)
            _events.emit(HomeEvent.TaskCompleted(task))
        }
    }

    fun reopenTask(task: Task) {
        viewModelScope.launch { taskRepository.reopenTask(task.id) }
    }

    /** Deleting is only allowed outside a session so tasks can't be dodged mid-focus. */
    fun deleteTask(task: Task) {
        if (sessionState.value.isFocusActive) return
        viewModelScope.launch {
            taskRepository.deleteTask(task.id)
            _events.emit(HomeEvent.TaskDeleted(task))
        }
    }

    fun restoreTask(task: Task) {
        viewModelScope.launch { taskRepository.restoreTask(task) }
    }

    fun startFocusSession() {
        if (focusSessionRepository.startFocusSession()) {
            FocusMonitorService.start(appContext)
        }
    }

    fun stopFocusSession() {
        focusSessionRepository.stopFocusSession()
        FocusMonitorService.stop(appContext)
    }

    /** Makes sure monitoring runs for a session restored after the app was killed. */
    fun ensureMonitoring() {
        if (sessionState.value.isFocusActive) {
            FocusMonitorService.start(appContext)
        }
    }
}
