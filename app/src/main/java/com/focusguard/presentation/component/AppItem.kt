package com.focusguard.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.focusguard.presentation.theme.AcidGreen
import com.focusguard.presentation.theme.DarkBackground
import com.focusguard.presentation.theme.DarkOnSurface
import com.focusguard.presentation.theme.DarkOnSurfaceMuted
import com.focusguard.presentation.theme.DarkSurface

@Composable
fun AppItem(
    appName: String,
    packageName: String,
    isBlocked: Boolean,
    onToggleBlocked: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(DarkSurface)
            .clickable { onToggleBlocked(!isBlocked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = appName,
                style = MaterialTheme.typography.titleMedium,
                color = DarkOnSurface
            )
            Text(
                text = packageName,
                style = MaterialTheme.typography.labelSmall,
                color = DarkOnSurfaceMuted
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Checkbox(
            checked = isBlocked,
            onCheckedChange = { onToggleBlocked(it) },
            colors = CheckboxDefaults.colors(
                checkedColor = AcidGreen,
                checkmarkColor = DarkBackground,
                uncheckedColor = DarkOnSurfaceMuted
            )
        )
    }
}
