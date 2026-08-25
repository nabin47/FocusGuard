package com.focusguard.presentation.screen.appselect

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import androidx.hilt.navigation.compose.hiltViewModel
import com.focusguard.presentation.component.AppItem
import com.focusguard.presentation.theme.AcidGreen
import com.focusguard.presentation.theme.DarkBackground
import com.focusguard.presentation.theme.DarkDivider
import com.focusguard.presentation.theme.DarkOnSurface
import com.focusguard.presentation.theme.DarkOnSurfaceMuted

@Composable
fun AppSelectScreen(
    onNavigateBack: () -> Unit,
    viewModel: AppSelectViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val appList by viewModel.appList.collectAsState()

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBackground)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = onNavigateBack,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AcidGreen,
                        contentColor = DarkBackground
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save & Return", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Blocked Apps",
                    style = MaterialTheme.typography.displayLarge,
                    color = DarkOnSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Select apps that will be blocked during an active focus session.",
                style = MaterialTheme.typography.bodyLarge,
                color = DarkOnSurfaceMuted
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text("Search installed apps...", color = DarkOnSurfaceMuted) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AcidGreen,
                    unfocusedBorderColor = DarkDivider,
                    focusedTextColor = DarkOnSurface,
                    unfocusedTextColor = DarkOnSurface
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (appList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No matching installed apps found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkOnSurfaceMuted
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(
                        items = appList,
                        key = { it.packageName }
                    ) { item ->
                        AppItem(
                            appName = item.appName,
                            packageName = item.packageName,
                            isBlocked = item.isBlocked,
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
