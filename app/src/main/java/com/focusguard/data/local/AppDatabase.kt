package com.focusguard.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.focusguard.data.local.dao.BlockedAppDao
import com.focusguard.data.local.dao.FocusSessionDao
import com.focusguard.data.local.dao.TaskDao
import com.focusguard.data.local.entity.BlockedAppEntity
import com.focusguard.data.local.entity.FocusSessionEntity
import com.focusguard.data.local.entity.TaskEntity

@Database(
    entities = [TaskEntity::class, BlockedAppEntity::class, FocusSessionEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun blockedAppDao(): BlockedAppDao
    abstract fun focusSessionDao(): FocusSessionDao

    companion object {
        const val NAME = "focusguard.db"

        /** v2 adds the focus_sessions table used for time tracking. Existing data is kept. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `focus_sessions` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`startedAt` INTEGER NOT NULL, " +
                        "`endedAt` INTEGER, " +
                        "`tasksCompleted` INTEGER NOT NULL, " +
                        "`blockedAttempts` INTEGER NOT NULL, " +
                        "`completedAllTasks` INTEGER NOT NULL)"
                )
            }
        }
    }
}
