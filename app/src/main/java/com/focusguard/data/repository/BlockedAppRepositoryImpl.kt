package com.focusguard.data.repository

import com.focusguard.data.local.dao.BlockedAppDao
import com.focusguard.data.local.entity.BlockedAppEntity
import com.focusguard.domain.model.BlockedApp
import com.focusguard.domain.repository.BlockedAppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BlockedAppRepositoryImpl @Inject constructor(
    private val blockedAppDao: BlockedAppDao
) : BlockedAppRepository {

    override fun getBlockedApps(): Flow<List<BlockedApp>> {
        return blockedAppDao.getBlockedApps().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addBlockedApp(packageName: String, appName: String) {
        blockedAppDao.insertBlockedApp(BlockedAppEntity(packageName = packageName, appName = appName))
    }

    override suspend fun removeBlockedApp(packageName: String) {
        blockedAppDao.deleteBlockedApp(packageName)
    }

    private fun BlockedAppEntity.toDomain(): BlockedApp = BlockedApp(
        packageName = packageName,
        appName = appName,
        addedAt = addedAt
    )
}
