package com.focusguard.presentation.screen.blocked

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.focusguard.MainActivity
import com.focusguard.domain.util.formatClock
import com.focusguard.presentation.component.AppIcon
import com.focusguard.presentation.component.PrimaryButton
import com.focusguard.presentation.component.SectionHeader
import com.focusguard.presentation.component.TaskItem
import com.focusguard.presentation.component.rememberCurrentTime
import com.focusguard.presentation.screen.home.HomeViewModel
import com.focusguard.presentation.theme.FocusGuardTheme
import com.focusguard.presentation.util.appLabel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BlockedOverlayActivity : ComponentActivity() {

    companion object {
        const val EXTRA_BLOCKED_PACKAGE = "extra_blocked_package"
    }

    private val homeViewModel: HomeViewModel by viewModels()
    private var blockedPackage by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        blockedPackage = intent?.getStringExtra(EXTRA_BLOCKED_PACKAGE)

        // Disable back button gesture/press to prevent escaping blocked state
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Do nothing - trap user until they return to FocusGuard and complete tasks
            }
        })

        setContent {
            FocusGuardTheme {
                BlockedOverlayScreen(
                    viewModel = homeViewModel,
                    blockedPackage = blockedPackage,
                    onReturnToFocusGuard = {
                        val intent = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        startActivity(intent)
                        finish()
                    },
                    onGoHome = {
                        val intent = Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_HOME)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(intent)
                        finish()
                    },
                    onAutoFinish = {
                        finish()
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.getStringExtra(EXTRA_BLOCKED_PACKAGE)?.let { blockedPackage = it }
    }
}

@Composable
fun BlockedOverlayScreen(
    viewModel: HomeViewModel,
    blockedPackage: String?,
    onReturnToFocusGuard: () -> Unit,
    onGoHome: () -> Unit,
    onAutoFinish: () -> Unit
) {
    val context = LocalContext.current
    val tasks by viewModel.tasks.collectAsState()
    val sessionState by viewModel.sessionState.collectAsState()
    val now = rememberCurrentTime(ticking = sessionState.isFocusActive)
    val colors = MaterialTheme.colorScheme
    val blockedName = remember(blockedPackage) { blockedPackage?.let { appLabel(context, it) } }

    // Auto dismiss overlay when focus session turns inactive (e.g. all tasks marked complete).
    // Wait for the session to load so a freshly started process doesn't close it too early.
    LaunchedEffect(sessionState.isLoaded, sessionState.isFocusActive) {
        if (sessionState.isLoaded && !sessionState.isFocusActive) {
            onAutoFinish()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        if (blockedPackage != null && blockedName != null) {
            AppIcon(packageName = blockedPackage, appName = blockedName)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = "APP BLOCKED",
            style = MaterialTheme.typography.labelSmall,
            color = colors.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Stay Focused",
            style = MaterialTheme.typography.displayLarge,
            color = colors.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${blockedName ?: "This app"} is locked until your tasks are done.",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        sessionState.startedAt?.let { startedAt ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Focused for ${formatClock(now - startedAt)}",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionHeader(title = "Remaining tasks (${tasks.size}) · tap to complete")

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(
                items = tasks,
                key = { it.id }
            ) { task ->
                TaskItem(
                    task = task,
                    onCompleteToggle = viewModel::completeTask
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        PrimaryButton(
            text = "Go back and focus",
            onClick = onReturnToFocusGuard
        )

        Spacer(modifier = Modifier.height(4.dp))

        TextButton(onClick = onGoHome, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Home, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Go to home screen", color = colors.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
