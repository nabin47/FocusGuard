package com.focusguard.presentation.screen.blocked

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusguard.MainActivity
import com.focusguard.presentation.component.TaskItem
import com.focusguard.presentation.screen.home.HomeViewModel
import com.focusguard.presentation.theme.AcidGreen
import com.focusguard.presentation.theme.DarkBackground
import com.focusguard.presentation.theme.DarkOnSurface
import com.focusguard.presentation.theme.DarkOnSurfaceMuted
import com.focusguard.presentation.theme.FocusGuardTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BlockedOverlayActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                    onReturnToFocusGuard = {
                        val intent = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun BlockedOverlayScreen(
    viewModel: HomeViewModel,
    onReturnToFocusGuard: () -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "APP BLOCKED",
                color = AcidGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Stay Focused",
                style = MaterialTheme.typography.displayLarge,
                color = DarkOnSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "You locked this app until your active tasks are marked complete.",
                style = MaterialTheme.typography.bodyLarge,
                color = DarkOnSurfaceMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "REMAINING TASKS (${tasks.size})",
                style = MaterialTheme.typography.labelSmall,
                color = DarkOnSurfaceMuted,
                letterSpacing = 1.5.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

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

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onReturnToFocusGuard,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AcidGreen,
                    contentColor = DarkBackground
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "GO BACK AND FOCUS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
