package com.focusguard.domain.usecase

import com.focusguard.domain.repository.BlockedAppRepository
import javax.inject.Inject

class RemoveBlockedAppUseCase @Inject constructor(
    private val blockedAppRepository: BlockedAppRepository
) {
    suspend operator fun invoke(packageName: String) {
        blockedAppRepository.removeBlockedApp(packageName)
    }
}
