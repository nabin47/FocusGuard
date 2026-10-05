package com.focusguard.presentation.screen.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.focusguard.domain.model.BlockedApp
import com.focusguard.domain.model.FocusStats
import com.focusguard.domain.model.Task
import com.focusguard.domain.repository.FocusSessionState
import com.focusguard.domain.util.formatClock
import com.focusguard.domain.util.formatDuration
import com.focusguard.presentation.component.AppTextField
import com.focusguard.presentation.component.EmptyState
import com.focusguard.presentation.component.NoticeCard
import com.focusguard.presentation.component.OnResume
import com.focusguard.presentation.component.PrimaryButton
import com.focusguard.presentation.component.PulsingDot
import com.focusguard.presentation.component.SecondaryButton
import com.focusguard.presentation.component.SectionHeader
import com.focusguard.presentation.component.TaskItem
import com.focusguard.presentation.component.rememberCurrentTime
import com.focusguard.presentation.util.hasRequiredPermissions
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onNavigateToAppSelect: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val sessionState by viewModel.sessionState.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val completedToday by viewModel.completedToday.collectAsState()
    val stats by viewModel.stats.collectAsState()

    var showAddTaskSheet by remember { mutableStateOf(false) }
    var showStopDialog by remember { mutableStateOf(false) }
    var permissionsGranted by remember { mutableStateOf(hasRequiredPermissions(context)) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    OnResume {
        permissionsGranted = hasRequiredPermissions(context)
        viewModel.ensureMonitoring()
    }

    LaunchedEffect(sessionState.isFocusActive) {
        if (sessionState.isFocusActive) viewModel.ensureMonitoring()
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                when (event) {
                    is HomeEvent.TaskCompleted -> {
                        val result = snackbarHostState.showSnackbar(
                            message = "Completed “${event.task.title}”",
                            actionLabel = "Undo",
                            duration = SnackbarDuration.Short
                        )
                        if (result == SnackbarResult.ActionPerformed) viewModel.reopenTask(event.task)
                    }
                    is HomeEvent.TaskDeleted -> {
                        val result = snackbarHostState.showSnackbar(
                            message = "Task deleted",
                            actionLabel = "Undo",
                            duration = SnackbarDuration.Short
                        )
                        if (result == SnackbarResult.ActionPerformed) viewModel.restoreTask(event.task)
                    }
                    is HomeEvent.SessionEnded -> {
                        val session = event.session
                        val duration = formatDuration(session.durationMillis())
                        val message = if (session.completedAllTasks) {
                            "All tasks done! You focused for $duration."
                        } else {
                            "Session ended after $duration."
                        }
                        snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Long)
                    }
                }
            }
        }
    }

    // Tasks finished since this session started, used for the progress bar.
    val sessionStart = sessionState.startedAt
    val doneThisSession = if (sessionStart == null) 0 else completedToday.count { (it.completedAt ?: 0) >= sessionStart }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                if (sessionState.isFocusActive) {
                    SecondaryButton(
                        text = "End session",
                        icon = Icons.Filled.Close,
                        onClick = {
                            if (tasks.isNotEmpty()) showStopDialog = true else viewModel.stopFocusSession()
                        }
                    )
                } else {
                    PrimaryButton(
                        text = if (tasks.isEmpty()) "Add a task to start" else "Start focus session",
                        icon = Icons.Filled.PlayArrow,
                        enabled = tasks.isNotEmpty(),
                        onClick = viewModel::startFocusSession
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "header") {
                HomeHeader(onNavigateToStats = onNavigateToStats)
            }

            if (!permissionsGranted) {
                item(key = "permissions") {
                    NoticeCard(
                        icon = Icons.Filled.Warning,
                        text = "Some permissions are missing, so apps can't be blocked. Tap to fix.",
                        onClick = onNavigateToPermissions
                    )
                }
            }

            item(key = "hero") {
                FocusHeroCard(
                    state = sessionState,
                    stats = stats,
                    doneThisSession = doneThisSession
                )
            }

            item(key = "apps") {
                BlockedAppsCard(apps = sessionState.blockedApps, onClick = onNavigateToAppSelect)
            }

            item(key = "tasksHeader") {
                SectionHeader(
                    title = "Tasks · ${tasks.size}",
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    TextButton(onClick = { showAddTaskSheet = true }) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add task")
                    }
                }
            }

            if (tasks.isEmpty()) {
                item(key = "empty") {
                    EmptyState(
                        icon = Icons.Filled.CheckCircle,
                        title = if (completedToday.isEmpty()) "No tasks yet" else "All clear!",
                        message = if (completedToday.isEmpty()) {
                            "Add what you need to get done. Your blocked apps stay locked until every task is checked off."
                        } else {
                            "Everything is done. Add another task to start a new session."
                        }
                    )
                }
            } else {
                items(items = tasks, key = { "task-${it.id}" }) { task ->
                    TaskItem(
                        task = task,
                        onCompleteToggle = viewModel::completeTask,
                        onDelete = if (sessionState.isFocusActive) null else viewModel::deleteTask,
                        modifier = Modifier.animateItemPlacement()
                    )
                }
            }

            if (completedToday.isNotEmpty()) {
                item(key = "doneHeader") {
                    SectionHeader(
                        title = "Done today · ${completedToday.size}",
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(items = completedToday, key = { "done-${it.id}" }) { task ->
                    TaskItem(
                        task = task,
                        subtitle = task.completedAt?.let { "Completed at ${formatTime(it)}" },
                        onCompleteToggle = viewModel::reopenTask,
                        modifier = Modifier.animateItemPlacement()
                    )
                }
            }
        }
    }

    if (showAddTaskSheet) {
        AddTaskSheet(
            onAdd = viewModel::addTask,
            onDismiss = { showAddTaskSheet = false }
        )
    }

    if (showStopDialog) {
        AlertDialog(
            onDismissRequest = { showStopDialog = false },
            title = { Text("End session early?") },
            text = {
                Text(
                    "You still have ${tasks.size} task${if (tasks.size == 1) "" else "s"} left. " +
                        "Your blocked apps will be unlocked. The time you've focused so far is still saved."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showStopDialog = false
                    viewModel.stopFocusSession()
                }) {
                    Text("End session", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStopDialog = false }) {
                    Text("Keep focusing")
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun HomeHeader(onNavigateToStats: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM")).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "FocusGuard",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        IconButton(
            onClick = onNavigateToStats,
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Icon(
                imageVector = Icons.Filled.DateRange,
                contentDescription = "Focus statistics",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun FocusHeroCard(
    state: FocusSessionState,
    stats: FocusStats,
    doneThisSession: Int
) {
    val colors = MaterialTheme.colorScheme
    val active = state.isFocusActive
    val now = rememberCurrentTime(ticking = active)
    val shape = MaterialTheme.shapes.large

    val background = if (active) {
        Brush.verticalGradient(listOf(colors.primaryContainer, colors.surface))
    } else {
        Brush.verticalGradient(listOf(colors.surface, colors.surface))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .clip(shape)
            .background(background)
            .border(1.dp, if (active) colors.primary.copy(alpha = 0.5f) else colors.outline, shape)
            .padding(20.dp)
    ) {
        AnimatedContent(targetState = active, label = "hero") { isActive ->
            if (isActive) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PulsingDot(color = colors.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("FOCUS ACTIVE", style = MaterialTheme.typography.labelSmall, color = colors.primary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatClock(now - (state.startedAt ?: now)),
                        style = MaterialTheme.typography.displayMedium,
                        color = colors.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val total = doneThisSession + state.activeTasks.size
                    LinearProgressIndicator(
                        progress = { if (total == 0) 0f else doneThisSession.toFloat() / total },
                        color = colors.primary,
                        trackColor = colors.surfaceVariant,
                        strokeCap = StrokeCap.Round,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "$doneThisSession of $total tasks done · ${state.blockedAttempts} distraction" +
                            "${if (state.blockedAttempts == 1) "" else "s"} blocked",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant
                    )
                }
            } else {
                Column {
                    Text("Ready to focus?", style = MaterialTheme.typography.titleLarge, color = colors.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when {
                            state.activeTasks.isEmpty() -> "Add a task, pick the apps that distract you, then start a session."
                            state.blockedApps.isEmpty() -> "No apps selected yet — choose which apps to block below."
                            else -> "${state.blockedApps.size} app${if (state.blockedApps.size == 1) "" else "s"} " +
                                "will stay locked until your ${state.activeTasks.size} " +
                                "task${if (state.activeTasks.size == 1) " is" else "s are"} done."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        HeroStat(label = "Today", value = formatDuration(stats.todayFocusMillis))
                        HeroStat(label = "Sessions", value = stats.todaySessions.toString())
                        HeroStat(
                            label = "Streak",
                            value = "${stats.currentStreakDays} day${if (stats.currentStreakDays == 1) "" else "s"}"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroStat(label: String, value: String) {
    Column {
        Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun BlockedAppsCard(apps: List<BlockedApp>, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        color = colors.surface,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Lock, contentDescription = null, tint = colors.primary)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Blocked apps · ${apps.size}", style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
                Text(
                    text = if (apps.isEmpty()) "None selected — tap to choose" else blockedAppsSummary(apps),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Manage blocked apps",
                tint = colors.onSurfaceVariant
            )
        }
    }
}

private fun blockedAppsSummary(apps: List<BlockedApp>): String {
    val shown = apps.take(3).joinToString { it.appName }
    return if (apps.size > 3) "$shown +${apps.size - 3}" else shown
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTaskSheet(
    onAdd: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    var title by remember { mutableStateOf("") }
    var addedCount by remember { mutableIntStateOf(0) }

    fun closeSheet() {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            Text("New task", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (addedCount > 0) "$addedCount added. Keep going, or tap Done." else "Press Enter on the keyboard to add several in a row.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            AppTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = "What do you need to get done?",
                onImeAction = {
                    if (title.isNotBlank()) {
                        onAdd(title)
                        title = ""
                        addedCount++
                    }
                },
                modifier = Modifier.focusRequester(focusRequester)
            )
            Spacer(modifier = Modifier.height(20.dp))
            PrimaryButton(
                text = if (title.isBlank() && addedCount > 0) "Done" else "Add task",
                enabled = title.isNotBlank() || addedCount > 0,
                onClick = {
                    if (title.isNotBlank()) onAdd(title)
                    closeSheet()
                }
            )
        }
        LaunchedEffect(Unit) {
            // Open the keyboard straight away; ignore if the field isn't attached yet.
            runCatching { focusRequester.requestFocus() }
        }
    }
}

private fun formatTime(millis: Long): String =
    DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(millis))
