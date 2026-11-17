package app.ebbplanner.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import app.ebbplanner.ui.theme.EbbSpacing

@Composable
fun MinTouchTargetIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    IconButton(
        onClick = onClick,
        modifier =
            modifier.defaultMinSize(
                minWidth = EbbSpacing.touchMin,
                minHeight = EbbSpacing.touchMin,
            ),
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(),
    ) {
        Box(
            modifier = Modifier.size(EbbSpacing.touchMin),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.semantics { this.contentDescription = contentDescription },
            )
        }
    }
}
