package com.focusguard.data.repository

import com.focusguard.data.local.entity.BlockedAppEntity
import com.focusguard.data.local.entity.FocusSessionEntity
import com.focusguard.data.local.entity.TaskEntity
import com.focusguard.domain.model.BlockedApp
import com.focusguard.domain.model.FocusSession
import com.focusguard.domain.model.Task

internal fun TaskEntity.toDomain() = Task(
    id = id,
    title = title,
    isCompleted = isCompleted,
    createdAt = createdAt,
    completedAt = completedAt
)

internal fun Task.toEntity() = TaskEntity(
    id = id,
    title = title,
    isCompleted = isCompleted,
    createdAt = createdAt,
    completedAt = completedAt
)

internal fun BlockedAppEntity.toDomain() = BlockedApp(
    packageName = packageName,
    appName = appName,
    addedAt = addedAt
)

internal fun FocusSessionEntity.toDomain() = FocusSession(
    id = id,
    startedAt = startedAt,
    endedAt = endedAt,
    tasksCompleted = tasksCompleted,
    blockedAttempts = blockedAttempts,
    completedAllTasks = completedAllTasks
)
