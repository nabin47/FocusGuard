package com.focusguard.domain.usecase

import com.focusguard.domain.repository.BlockedAppRepository
import javax.inject.Inject

class AddBlockedAppUseCase @Inject constructor(
    private val blockedAppRepository: BlockedAppRepository
) {
    suspend operator fun invoke(packageName: String, appName: String) {
        blockedAppRepository.addBlockedApp(packageName, appName)
    }
}
