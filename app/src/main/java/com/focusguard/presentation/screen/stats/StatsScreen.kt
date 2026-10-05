package com.focusguard.presentation.screen.stats

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.focusguard.domain.model.DailyFocus
import com.focusguard.domain.model.FocusSession
import com.focusguard.domain.util.formatDuration
import com.focusguard.presentation.component.EmptyState
import com.focusguard.presentation.component.SectionHeader
import com.focusguard.presentation.component.StatTile
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun StatsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val stats by viewModel.stats.collectAsState()
    val colors = MaterialTheme.colorScheme

    Scaffold(containerColor = colors.background) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "header") {
                Column {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.onSurface)
                    }
                    Text("Your Focus", style = MaterialTheme.typography.displayLarge, color = colors.onBackground)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Time tracked across your focus sessions.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            item(key = "tilesTop") {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile("Focused today", formatDuration(stats.todayFocusMillis), Modifier.weight(1f))
                    StatTile("Last 7 days", formatDuration(stats.weekFocusMillis), Modifier.weight(1f))
                }
            }
            item(key = "tilesBottom") {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile(
                        "Day streak",
                        stats.currentStreakDays.toString(),
                        Modifier.weight(1f)
                    )
                    StatTile(
                        "Blocked today",
                        stats.todayBlockedAttempts.toString(),
                        Modifier.weight(1f)
                    )
                }
            }

            item(key = "chart") {
                WeeklyChart(days = stats.last7Days)
            }

            item(key = "historyHeader") {
                SectionHeader(
                    title = "Recent sessions · ${stats.totalSessions} total · ${formatDuration(stats.totalFocusMillis)}",
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (stats.recentSessions.isEmpty()) {
                item(key = "empty") {
                    EmptyState(
                        icon = Icons.Filled.DateRange,
                        title = "No sessions yet",
                        message = "Start a focus session and your time will show up here."
                    )
                }
            } else {
                items(items = stats.recentSessions, key = { it.id }) { session ->
                    SessionRow(session)
                }
            }
        }
    }
}

/** Single-series bar chart of focus time per day. Tap a bar to read its value. */
@Composable
private fun WeeklyChart(days: List<DailyFocus>) {
    val colors = MaterialTheme.colorScheme
    var selectedIndex by remember(days.size) { mutableStateOf(days.lastIndex) }
    val maxMillis = days.maxOfOrNull { it.focusMillis }?.coerceAtLeast(1L) ?: 1L
    val selected = days.getOrNull(selectedIndex)

    Surface(color = colors.surface, shape = MaterialTheme.shapes.medium) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = selected?.let { dayLabel(it.date) } ?: "Last 7 days",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
            Text(
                text = formatDuration(selected?.focusMillis ?: 0),
                style = MaterialTheme.typography.headlineSmall,
                color = colors.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                days.forEachIndexed { index, day ->
                    val fraction by animateFloatAsState(day.focusMillis.toFloat() / maxMillis, label = "bar")
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { selectedIndex = index }
                            .semantics {
                                contentDescription = "${dayLabel(day.date)}: ${formatDuration(day.focusMillis)}"
                            },
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .widthIn(max = 28.dp)
                                .fillMaxWidth(0.7f)
                                .fillMaxHeight(fraction.coerceAtLeast(0.02f))
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    when {
                                        day.focusMillis == 0L -> colors.surfaceVariant
                                        index == selectedIndex -> colors.primary
                                        else -> colors.primary.copy(alpha = 0.45f)
                                    }
                                )
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.outline)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                days.forEachIndexed { index, day ->
                    Text(
                        text = day.date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (index == selectedIndex) colors.onSurface else colors.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionRow(session: FocusSession) {
    val colors = MaterialTheme.colorScheme
    Surface(color = colors.surface, shape = MaterialTheme.shapes.medium) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sessionDateFormatter.format(Instant.ofEpochMilli(session.startedAt).atZone(ZoneId.systemDefault())),
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    when {
                        session.isActive -> Text("In progress", style = MaterialTheme.typography.bodyMedium, color = colors.primary)
                        session.completedAllTasks -> {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("All tasks done", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                        }
                        else -> Text("Ended early", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                    }
                    if (!session.isActive) {
                        Text(
                            text = " · ${session.tasksCompleted} task${if (session.tasksCompleted == 1) "" else "s"} · " +
                                "${session.blockedAttempts} blocked",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }
            Text(
                text = formatDuration(session.durationMillis()),
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface
            )
        }
    }
}

private val sessionDateFormatter = DateTimeFormatter.ofPattern("EEE d MMM · HH:mm")

private fun dayLabel(date: LocalDate): String {
    val today = LocalDate.now()
    return when (date) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> date.format(DateTimeFormatter.ofPattern("EEEE, d MMM"))
    }
}
