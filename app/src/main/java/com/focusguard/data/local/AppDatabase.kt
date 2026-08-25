package com.focusguard.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.focusguard.data.local.dao.BlockedAppDao
import com.focusguard.data.local.dao.TaskDao
import com.focusguard.data.local.entity.BlockedAppEntity
import com.focusguard.data.local.entity.TaskEntity

@Database(
    entities = [TaskEntity::class, BlockedAppEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun blockedAppDao(): BlockedAppDao
}
