package com.focusguard.presentation.screen.appselect

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusguard.domain.repository.BlockedAppRepository
import com.focusguard.domain.repository.FocusSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InstalledAppItem(
    val appName: String,
    val packageName: String,
    val isBlocked: Boolean
)

data class AppSelectUiState(
    val isLoading: Boolean = true,
    val apps: List<InstalledAppItem> = emptyList(),
    val blockedCount: Int = 0,
    /** During a session apps can be added to the block list but not removed. */
    val isFocusActive: Boolean = false
)

@HiltViewModel
class AppSelectViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val blockedAppRepository: BlockedAppRepository,
    focusSessionRepository: FocusSessionRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // null until the installed apps have been loaded.
    private val installedApps = MutableStateFlow<List<Pair<String, String>>?>(null)

    val uiState: StateFlow<AppSelectUiState> = combine(
        installedApps,
        blockedAppRepository.getBlockedApps(),
        _searchQuery,
        focusSessionRepository.sessionState
    ) { installed, blocked, query, session ->
        val blockedPackages = blocked.map { it.packageName }.toSet()
        val apps = installed.orEmpty()
            .filter { (name, pkg) ->
                query.isBlank() || name.contains(query, ignoreCase = true) || pkg.contains(query, ignoreCase = true)
            }
            .map { (name, pkg) -> InstalledAppItem(name, pkg, isBlocked = pkg in blockedPackages) }
        AppSelectUiState(
            isLoading = installed == null,
            apps = apps,
            blockedCount = blockedPackages.size,
            isFocusActive = session.isFocusActive
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSelectUiState())

    init {
        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
            val ownPackageName = context.packageName

            installedApps.value = pm.queryIntentActivities(mainIntent, 0)
                .mapNotNull { resolveInfo ->
                    val appInfo = resolveInfo.activityInfo.applicationInfo
                    val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    if (appInfo.packageName == ownPackageName || isSystemApp) return@mapNotNull null
                    pm.getApplicationLabel(appInfo).toString() to appInfo.packageName
                }
                .distinctBy { it.second }
                .sortedBy { it.first.lowercase() }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun toggleAppBlocked(packageName: String, appName: String, shouldBlock: Boolean) {
        if (!shouldBlock && uiState.value.isFocusActive) return
        viewModelScope.launch {
            if (shouldBlock) {
                blockedAppRepository.addBlockedApp(packageName, appName)
            } else {
                blockedAppRepository.removeBlockedApp(packageName)
            }
        }
    }
}
