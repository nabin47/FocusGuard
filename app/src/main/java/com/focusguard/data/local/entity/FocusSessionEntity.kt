package com.focusguard.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAt: Long,
    val endedAt: Long? = null,
    val tasksCompleted: Int = 0,
    val blockedAttempts: Int = 0,
    val completedAllTasks: Boolean = false
)
