package com.focusguard.data.repository

import com.focusguard.domain.model.FocusSession
import com.focusguard.domain.model.Task
import com.focusguard.fakes.FakeBlockedAppRepository
import com.focusguard.fakes.FakeSessionHistoryRepository
import com.focusguard.fakes.FakeTaskRepository
import com.focusguard.fakes.FakeTimeProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FocusSessionRepositoryImplTest {

    private val time = FakeTimeProvider(now = 1_000L)
    private val tasks = FakeTaskRepository(time)
    private val apps = FakeBlockedAppRepository()
    private val history = FakeSessionHistoryRepository()

    private fun TestScope.createRepository() = FocusSessionRepositoryImpl(
        taskRepository = tasks,
        blockedAppRepository = apps,
        historyRepository = history,
        timeProvider = time,
        scope = backgroundScope
    )

    @Test
    fun `start and stop focus session toggles focus state`() = runTest(UnconfinedTestDispatcher()) {
        tasks.addTask("Write essay")
        val repository = createRepository()
        assertTrue(repository.sessionState.value.isLoaded)
        assertFalse(repository.sessionState.value.isFocusActive)

        assertTrue(repository.startFocusSession())
        assertTrue(repository.sessionState.value.isFocusActive)
        assertEquals(1_000L, repository.sessionState.value.startedAt)

        repository.stopFocusSession()
        assertFalse(repository.sessionState.value.isFocusActive)
        assertNull(repository.sessionState.value.startedAt)
    }

    @Test
    fun `cannot start a session without tasks`() = runTest(UnconfinedTestDispatcher()) {
        val repository = createRepository()
        assertFalse(repository.startFocusSession())
        assertFalse(repository.sessionState.value.isFocusActive)
        assertTrue(history.sessions.value.isEmpty())
    }

    @Test
    fun `completing all tasks auto stops active focus session`() = runTest(UnconfinedTestDispatcher()) {
        val id = tasks.addTask("Task 1")
        val repository = createRepository()
        repository.startFocusSession()
        assertTrue(repository.sessionState.value.isFocusActive)

        time.now = 61_000L
        tasks.completeTask(id)

        assertFalse(repository.sessionState.value.isFocusActive)
        val session = history.sessions.value.single()
        assertEquals(61_000L, session.endedAt)
        assertEquals(60_000L, session.durationMillis())
        assertEquals(1, session.tasksCompleted)
        assertTrue(session.completedAllTasks)
    }

    @Test
    fun `stopping early is recorded as not completed`() = runTest(UnconfinedTestDispatcher()) {
        tasks.addTask("Task 1")
        tasks.addTask("Task 2")
        val repository = createRepository()
        val ended = mutableListOf<FocusSession>()
        backgroundScope.launch { repository.endedSessions.collect { ended += it } }

        repository.startFocusSession()
        time.now = 5_000L
        repository.stopFocusSession()

        val session = history.sessions.value.single()
        assertFalse(session.completedAllTasks)
        assertEquals(0, session.tasksCompleted)
        assertEquals(4_000L, session.durationMillis())
        assertEquals(1, ended.size)
    }

    @Test
    fun `blocked attempts are counted and persisted`() = runTest(UnconfinedTestDispatcher()) {
        tasks.addTask("Task 1")
        val repository = createRepository()

        repository.recordBlockedAttempt() // ignored: no session yet
        repository.startFocusSession()
        repository.recordBlockedAttempt()
        repository.recordBlockedAttempt()

        assertEquals(2, repository.sessionState.value.blockedAttempts)
        assertEquals(2, history.sessions.value.single().blockedAttempts)

        repository.stopFocusSession()
        assertEquals(2, history.sessions.value.single().blockedAttempts)
        assertEquals(0, repository.sessionState.value.blockedAttempts)
    }

    @Test
    fun `active session is restored after restart`() = runTest(UnconfinedTestDispatcher()) {
        tasks.addTask("Task 1")
        history.startSession(startedAt = 500L)
        history.updateBlockedAttempts(3)

        val repository = createRepository()

        val state = repository.sessionState.value
        assertTrue(state.isFocusActive)
        assertEquals(500L, state.startedAt)
        assertEquals(3, state.blockedAttempts)
    }

    @Test
    fun `blocked apps are synced and matched only during a session`() = runTest(UnconfinedTestDispatcher()) {
        tasks.addTask("Task 1")
        apps.addBlockedApp("com.instagram.android", "Instagram")
        val repository = createRepository()

        assertEquals("com.instagram.android", repository.sessionState.value.blockedApps.single().packageName)
        assertFalse(repository.sessionState.value.isBlocked("com.instagram.android"))

        repository.startFocusSession()
        assertTrue(repository.sessionState.value.isBlocked("com.instagram.android"))
        assertFalse(repository.sessionState.value.isBlocked("com.example.notes"))
    }

    @Test
    fun `active tasks are synced from the task repository`() = runTest(UnconfinedTestDispatcher()) {
        val repository = createRepository()
        tasks.addTask("Task 1")
        tasks.restoreTask(Task(id = 42, title = "Restored"))

        assertEquals(2, repository.sessionState.value.activeTasks.size)
    }
}
