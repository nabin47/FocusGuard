package com.focusguard.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.focusguard.domain.model.Task
import com.focusguard.presentation.theme.AcidGreen
import com.focusguard.presentation.theme.DarkBackground
import com.focusguard.presentation.theme.DarkOnSurface
import com.focusguard.presentation.theme.DarkOnSurfaceMuted
import com.focusguard.presentation.theme.DarkSurface

@Composable
fun TaskItem(
    task: Task,
    onCompleteToggle: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(DarkSurface)
            .clickable { onCompleteToggle(task) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = task.isCompleted,
            onCheckedChange = { onCompleteToggle(task) },
            colors = CheckboxDefaults.colors(
                checkedColor = AcidGreen,
                checkmarkColor = DarkBackground,
                uncheckedColor = DarkOnSurfaceMuted
            )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = task.title,
            color = if (task.isCompleted) DarkOnSurfaceMuted else DarkOnSurface,
            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
            modifier = Modifier.weight(1f)
        )
    }
}
