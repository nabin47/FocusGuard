package com.focusguard.presentation.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusguard.presentation.theme.AcidGreen
import com.focusguard.presentation.theme.DarkBackground
import com.focusguard.presentation.theme.DarkDivider
import com.focusguard.presentation.theme.DarkOnSurfaceMuted

@Composable
fun FocusStatusBanner(
    isFocusActive: Boolean,
    remainingTasksCount: Int,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(50.dp)

    if (isFocusActive) {
        Surface(
            color = AcidGreen,
            shape = shape,
            modifier = modifier
                .fillMaxWidth()
                .clip(shape)
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "● FOCUS ACTIVE — $remainingTasksCount task${if (remainingTasksCount != 1) "s" else ""} remaining",
                    color = DarkBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = DarkDivider, shape = shape)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "○ FOCUS IDLE — Tap 'Start Focus' to lock distractions",
                color = DarkOnSurfaceMuted,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}
