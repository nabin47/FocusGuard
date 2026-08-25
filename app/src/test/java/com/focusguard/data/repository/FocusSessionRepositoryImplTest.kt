package com.focusguard.data.repository

import com.focusguard.domain.model.BlockedApp
import com.focusguard.domain.model.Task
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FocusSessionRepositoryImplTest {

    @Test
    fun `start and stop focus session toggles focus state`() = runTest {
        val repository = FocusSessionRepositoryImpl()
        assertFalse(repository.sessionState.value.isFocusActive)

        repository.startFocusSession()
        assertTrue(repository.sessionState.value.isFocusActive)

        repository.stopFocusSession()
        assertFalse(repository.sessionState.value.isFocusActive)
    }

    @Test
    fun `updating active tasks to empty auto stops active focus session`() = runTest {
        val repository = FocusSessionRepositoryImpl()
        repository.startFocusSession()
        repository.updateActiveTasks(listOf(Task(id = 1, title = "Task 1")))
        assertTrue(repository.sessionState.value.isFocusActive)

        repository.updateActiveTasks(emptyList())
        assertFalse(repository.sessionState.value.isFocusActive)
    }

    @Test
    fun `updating blocked apps updates state flow`() = runTest {
        val repository = FocusSessionRepositoryImpl()
        val apps = listOf(BlockedApp(packageName = "com.instagram.android", appName = "Instagram"))
        repository.updateBlockedApps(apps)

        assertEquals(1, repository.sessionState.value.blockedApps.size)
        assertEquals("com.instagram.android", repository.sessionState.value.blockedApps[0].packageName)
    }
}
