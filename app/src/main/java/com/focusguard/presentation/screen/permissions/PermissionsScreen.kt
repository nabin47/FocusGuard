package com.focusguard.presentation.screen.permissions

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.focusguard.presentation.component.OnResume
import com.focusguard.presentation.component.PrimaryButton
import com.focusguard.presentation.component.SecondaryButton
import com.focusguard.presentation.util.hasNotificationPermission
import com.focusguard.presentation.util.hasOverlayPermission
import com.focusguard.presentation.util.hasUsageStatsPermission

@Composable
fun PermissionsScreen(
    onAllPermissionsGranted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasUsageStats by remember { mutableStateOf(hasUsageStatsPermission(context)) }
    var hasOverlay by remember { mutableStateOf(hasOverlayPermission(context)) }
    var hasNotifications by remember { mutableStateOf(hasNotificationPermission(context)) }
    val allRequiredGranted = hasUsageStats && hasOverlay

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasNotifications = granted }

    // Re-check whenever the user comes back from the system settings.
    OnResume {
        hasUsageStats = hasUsageStatsPermission(context)
        hasOverlay = hasOverlayPermission(context)
        hasNotifications = hasNotificationPermission(context)
        if (hasUsageStats && hasOverlay) {
            onAllPermissionsGranted()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Let's set up FocusGuard",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Two system permissions let FocusGuard notice when a blocked app opens and cover it until your tasks are done. Nothing leaves your device.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(28.dp))

        PermissionStep(
            number = 1,
            title = "Usage Access",
            description = "See which app is currently open so distractions can be blocked.",
            granted = hasUsageStats,
            actionLabel = "Grant Usage Access",
            onAction = { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        PermissionStep(
            number = 2,
            title = "Display Over Other Apps",
            description = "Show the full-screen focus reminder on top of a blocked app.",
            granted = hasOverlay,
            actionLabel = "Grant Overlay Permission",
            onAction = {
                context.startActivity(
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                )
            }
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Spacer(modifier = Modifier.height(12.dp))
            PermissionStep(
                number = 3,
                title = "Notifications (optional)",
                description = "Shows a live session timer and remaining tasks in your notification shade.",
                granted = hasNotifications,
                actionLabel = "Allow Notifications",
                secondary = true,
                onAction = { notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        PrimaryButton(
            text = if (allRequiredGranted) "Continue to FocusGuard" else "Grant the permissions above to continue",
            enabled = allRequiredGranted,
            onClick = onAllPermissionsGranted
        )
    }
}

@Composable
private fun PermissionStep(
    number: Int,
    title: String,
    description: String,
    granted: Boolean,
    actionLabel: String,
    onAction: () -> Unit,
    secondary: Boolean = false
) {
    val colors = MaterialTheme.colorScheme
    Surface(color = colors.surface, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (granted) colors.primary else colors.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (granted) {
                        Icon(Icons.Filled.Check, contentDescription = "Granted", tint = colors.onPrimary, modifier = Modifier.size(18.dp))
                    } else {
                        Text(number.toString(), style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, color = colors.onSurface, modifier = Modifier.weight(1f))
                Text(
                    text = if (granted) "GRANTED" else if (secondary) "OPTIONAL" else "REQUIRED",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (granted) colors.primary else colors.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            if (!granted) {
                Spacer(modifier = Modifier.height(16.dp))
                if (secondary) {
                    SecondaryButton(text = actionLabel, onClick = onAction)
                } else {
                    PrimaryButton(text = actionLabel, onClick = onAction)
                }
            }
        }
    }
}
