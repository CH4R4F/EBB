@file:Suppress("NewLineAtEndOfFile")

package app.ebbplanner.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ebbplanner.ui.theme.EbbSymbol

@Composable
fun EbbListLeadingIcon(
    icon: String,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
) {
    Box(
        modifier = modifier.size(48.dp),
        contentAlignment = Alignment.Center,
    ) {
        EbbSymbol(
            name = icon,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            filled = filled,
        )
    }
}
