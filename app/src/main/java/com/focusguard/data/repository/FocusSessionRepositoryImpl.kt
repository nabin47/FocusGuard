package com.focusguard.data.repository

import com.focusguard.di.ApplicationScope
import com.focusguard.domain.model.FocusSession
import com.focusguard.domain.repository.BlockedAppRepository
import com.focusguard.domain.repository.FocusSessionRepository
import com.focusguard.domain.repository.FocusSessionState
import com.focusguard.domain.repository.SessionHistoryRepository
import com.focusguard.domain.repository.TaskRepository
import com.focusguard.domain.util.TimeProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FocusSessionRepositoryImpl @Inject constructor(
    private val taskRepository: TaskRepository,
    blockedAppRepository: BlockedAppRepository,
    private val historyRepository: SessionHistoryRepository,
    private val timeProvider: TimeProvider,
    @ApplicationScope scope: CoroutineScope
) : FocusSessionRepository {

    private val _sessionState = MutableStateFlow(FocusSessionState())
    override val sessionState: StateFlow<FocusSessionState> = _sessionState.asStateFlow()

    private val _endedSessions = MutableSharedFlow<FocusSession>(extraBufferCapacity = 8)
    override val endedSessions: SharedFlow<FocusSession> = _endedSessions.asSharedFlow()

    // Database writes are applied strictly in the order they were requested.
    private val pendingWrites = Channel<suspend () -> Unit>(Channel.UNLIMITED)

    init {
        scope.launch {
            for (write in pendingWrites) {
                try {
                    write()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    // A failed write must never take the session down with it.
                    e.printStackTrace()
                }
            }
        }

        scope.launch {
            // Resume a session that was running when the process died.
            val restored = historyRepository.getActiveSession()
            combine(taskRepository.getActiveTasks(), blockedAppRepository.getBlockedApps(), ::Pair)
                .collect { (tasks, apps) ->
                    val state = _sessionState.updateAndGet { current ->
                        val base = if (!current.isLoaded && restored != null) {
                            current.copy(
                                isFocusActive = true,
                                startedAt = restored.startedAt,
                                blockedAttempts = restored.blockedAttempts
                            )
                        } else {
                            current
                        }
                        base.copy(isLoaded = true, activeTasks = tasks, blockedApps = apps)
                    }
                    // All tasks done: the session has served its purpose.
                    if (state.isFocusActive && tasks.isEmpty()) {
                        endSession(completedAllTasks = true)
                    }
                }
        }
    }

    override fun startFocusSession(): Boolean {
        val startedAt = timeProvider.now()
        val previous = _sessionState.getAndUpdate { state ->
            if (state.canStart) {
                state.copy(isFocusActive = true, startedAt = startedAt, blockedAttempts = 0)
            } else {
                state
            }
        }
        if (previous.canStart) {
            enqueue { historyRepository.startSession(startedAt) }
            return true
        }
        return previous.isFocusActive
    }

    override fun stopFocusSession() = endSession(completedAllTasks = false)

    override fun recordBlockedAttempt() {
        val state = _sessionState.updateAndGet { state ->
            if (state.isFocusActive) state.copy(blockedAttempts = state.blockedAttempts + 1) else state
        }
        if (state.isFocusActive) {
            val attempts = state.blockedAttempts
            enqueue { historyRepository.updateBlockedAttempts(attempts) }
        }
    }

    private fun endSession(completedAllTasks: Boolean) {
        val previous = _sessionState.getAndUpdate { state ->
            if (state.isFocusActive) {
                state.copy(isFocusActive = false, startedAt = null, blockedAttempts = 0)
            } else {
                state
            }
        }
        if (!previous.isFocusActive) return

        val startedAt = previous.startedAt ?: timeProvider.now()
        val endedAt = timeProvider.now()
        val attempts = previous.blockedAttempts
        enqueue {
            val tasksCompleted = taskRepository.countCompletedBetween(startedAt, endedAt)
            historyRepository.endActiveSession(endedAt, tasksCompleted, attempts, completedAllTasks)
            _endedSessions.emit(
                FocusSession(
                    startedAt = startedAt,
                    endedAt = endedAt,
                    tasksCompleted = tasksCompleted,
                    blockedAttempts = attempts,
                    completedAllTasks = completedAllTasks
                )
            )
        }
    }

    private fun enqueue(write: suspend () -> Unit) {
        pendingWrites.trySend(write)
    }

    private val FocusSessionState.canStart: Boolean
        get() = isLoaded && !isFocusActive && activeTasks.isNotEmpty()
}
