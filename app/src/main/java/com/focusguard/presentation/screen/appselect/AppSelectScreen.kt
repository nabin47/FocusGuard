package com.focusguard.presentation.screen.appselect

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.focusguard.presentation.component.AppItem
import com.focusguard.presentation.component.AppTextField
import com.focusguard.presentation.component.EmptyState
import com.focusguard.presentation.component.NoticeCard
import com.focusguard.presentation.component.PrimaryButton

@Composable
fun AppSelectScreen(
    onNavigateBack: () -> Unit,
    viewModel: AppSelectViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val colors = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colors.background,
        bottomBar = {
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                PrimaryButton(
                    text = "Done · ${uiState.blockedCount} blocked",
                    onClick = onNavigateBack
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.onSurface)
                }
            }
            Text(
                text = "Blocked Apps",
                style = MaterialTheme.typography.displayLarge,
                color = colors.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "These apps are locked while a focus session is running.",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurfaceVariant
            )

            if (uiState.isFocusActive) {
                Spacer(modifier = Modifier.height(12.dp))
                NoticeCard(
                    icon = Icons.Filled.Info,
                    text = "A session is running: you can add apps, but unblocking waits until it ends.",
                    containerColor = colors.primaryContainer,
                    contentColor = colors.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                placeholder = "Search installed apps",
                leadingIcon = Icons.Filled.Search,
                imeAction = ImeAction.Search,
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                        }
                    }
                } else {
                    null
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                uiState.isLoading -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colors.primary)
                }

                uiState.apps.isEmpty() -> EmptyState(
                    icon = Icons.Filled.Search,
                    title = "No apps found",
                    message = if (searchQuery.isBlank()) "No launchable apps are installed." else "Nothing matches “$searchQuery”.",
                    modifier = Modifier.weight(1f)
                )

                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(items = uiState.apps, key = { it.packageName }) { item ->
                        AppItem(
                            appName = item.appName,
                            packageName = item.packageName,
                            isBlocked = item.isBlocked,
                            // Unblocking is locked during a session.
                            enabled = !(uiState.isFocusActive && item.isBlocked),
                            onToggleBlocked = { shouldBlock ->
                                viewModel.toggleAppBlocked(item.packageName, item.appName, shouldBlock)
                            }
                        )
                    }
                }
            }
        }
    }
}
