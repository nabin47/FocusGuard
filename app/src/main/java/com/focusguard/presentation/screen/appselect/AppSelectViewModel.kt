package com.focusguard.presentation.screen.appselect

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusguard.domain.model.BlockedApp
import com.focusguard.domain.usecase.AddBlockedAppUseCase
import com.focusguard.domain.usecase.GetBlockedAppsUseCase
import com.focusguard.domain.usecase.RemoveBlockedAppUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InstalledAppItem(
    val appName: String,
    val packageName: String,
    val isBlocked: Boolean
)

@HiltViewModel
class AppSelectViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getBlockedAppsUseCase: GetBlockedAppsUseCase,
    private val addBlockedAppUseCase: AddBlockedAppUseCase,
    private val removeBlockedAppUseCase: RemoveBlockedAppUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _installedApps = MutableStateFlow<List<Pair<String, String>>>(emptyList())

    val appList: StateFlow<List<InstalledAppItem>> = combine(
        _installedApps,
        getBlockedAppsUseCase(),
        _searchQuery
    ) { installed, blocked, query ->
        val blockedPackages = blocked.map { it.packageName }.toSet()
        installed
            .filter { (name, pkg) ->
                query.isBlank() || name.contains(query, ignoreCase = true) || pkg.contains(query, ignoreCase = true)
            }
            .map { (name, pkg) ->
                InstalledAppItem(
                    appName = name,
                    packageName = pkg,
                    isBlocked = blockedPackages.contains(pkg)
                )
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
            val ownPackageName = context.packageName

            val apps = resolveInfos
                .mapNotNull { resolveInfo ->
                    val appInfo = resolveInfo.activityInfo.applicationInfo
                    val packageName = appInfo.packageName
                    if (packageName == ownPackageName) return@mapNotNull null

                    // Filter system apps if desired (keep non-system or launchable)
                    val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    if (isSystemApp) return@mapNotNull null

                    val appName = pm.getApplicationLabel(appInfo).toString()
                    Pair(appName, packageName)
                }
                .distinctBy { it.second }
                .sortedBy { it.first.lowercase() }

            _installedApps.value = apps
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun toggleAppBlocked(packageName: String, appName: String, shouldBlock: Boolean) {
        viewModelScope.launch {
            if (shouldBlock) {
                addBlockedAppUseCase(packageName, appName)
            } else {
                removeBlockedAppUseCase(packageName)
            }
        }
    }
}
