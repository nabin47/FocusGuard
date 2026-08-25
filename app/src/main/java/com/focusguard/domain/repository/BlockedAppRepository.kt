package com.focusguard.domain.repository

import com.focusguard.domain.model.BlockedApp
import kotlinx.coroutines.flow.Flow

interface BlockedAppRepository {
    fun getBlockedApps(): Flow<List<BlockedApp>>
    suspend fun addBlockedApp(packageName: String, appName: String)
    suspend fun removeBlockedApp(packageName: String)
}
