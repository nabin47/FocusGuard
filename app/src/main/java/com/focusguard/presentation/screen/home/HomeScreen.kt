package com.focusguard.presentation.screen.home

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.focusguard.presentation.component.FocusStatusBanner
import com.focusguard.presentation.component.TaskItem
import com.focusguard.presentation.theme.AcidGreen
import com.focusguard.presentation.theme.AcidGreenDim
import com.focusguard.presentation.theme.DarkBackground
import com.focusguard.presentation.theme.DarkDivider
import com.focusguard.presentation.theme.DarkOnSurface
import com.focusguard.presentation.theme.DarkOnSurfaceMuted
import com.focusguard.presentation.theme.DarkSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAppSelect: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val tasks by viewModel.tasks.collectAsState()
    val sessionState by viewModel.sessionState.collectAsState()

    var showAddTaskSheet by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBackground)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateToAppSelect,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = DarkOnSurface
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Blocked Apps (${sessionState.blockedApps.size})",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = { showAddTaskSheet = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkSurface,
                            contentColor = DarkOnSurface
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+ Add Task", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.toggleFocusSession(context) },
                    enabled = tasks.isNotEmpty() || sessionState.isFocusActive,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (sessionState.isFocusActive) DarkSurface else AcidGreen,
                        contentColor = if (sessionState.isFocusActive) AcidGreen else DarkBackground,
                        disabledContainerColor = DarkSurface,
                        disabledContentColor = DarkOnSurfaceMuted
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (sessionState.isFocusActive) "STOP FOCUS SESSION" else "START FOCUS SESSION",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FocusGuard",
                    style = MaterialTheme.typography.displayLarge,
                    color = DarkOnSurface
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Focus Status Banner
            FocusStatusBanner(
                isFocusActive = sessionState.isFocusActive,
                remainingTasksCount = tasks.size
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "ACTIVE TASKS",
                style = MaterialTheme.typography.labelSmall,
                color = DarkOnSurfaceMuted,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterAlignment) {
                        Text(
                            text = "No active tasks",
                            style = MaterialTheme.typography.titleMedium,
                            color = DarkOnSurfaceMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add tasks to initiate a focus session.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DarkOnSurfaceMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(
                        items = tasks,
                        key = { it.id }
                    ) { task ->
                        TaskItem(
                            task = task,
                            onCompleteToggle = { viewModel.completeTask(it.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddTaskSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddTaskSheet = false },
            sheetState = sheetState,
            containerColor = DarkSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "New Focus Task",
                    style = MaterialTheme.typography.titleLarge,
                    color = DarkOnSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = newTaskTitle,
                    onValueChange = { newTaskTitle = it },
                    placeholder = { Text("What do you need to focus on?", color = DarkOnSurfaceMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AcidGreen,
                        unfocusedBorderColor = DarkDivider,
                        focusedTextColor = DarkOnSurface,
                        unfocusedTextColor = DarkOnSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (newTaskTitle.isNotBlank()) {
                            viewModel.addTask(newTaskTitle)
                            newTaskTitle = ""
                            showAddTaskSheet = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AcidGreen,
                        contentColor = DarkBackground
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Task", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
