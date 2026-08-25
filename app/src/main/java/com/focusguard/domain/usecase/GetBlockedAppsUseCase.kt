package com.focusguard.domain.usecase

import com.focusguard.domain.model.BlockedApp
import com.focusguard.domain.repository.BlockedAppRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBlockedAppsUseCase @Inject constructor(
    private val blockedAppRepository: BlockedAppRepository
) {
    operator fun invoke(): Flow<List<BlockedApp>> {
        return blockedAppRepository.getBlockedApps()
    }
}
