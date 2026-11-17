package app.ebbplanner.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.ebbplanner.R
import app.ebbplanner.ui.theme.EbbSpacing

@Composable
fun InboxPlaceholderScreen() {
    PlaceholderMessage(message = stringResource(R.string.tab_inbox_placeholder))
}

@Composable
fun WeekPlaceholderScreen() {
    PlaceholderMessage(message = stringResource(R.string.tab_week_placeholder))
}

@Composable
fun RoutinesPlaceholderScreen() {
    PlaceholderMessage(message = stringResource(R.string.tab_routines_placeholder))
}

@Composable
private fun PlaceholderMessage(message: String) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(EbbSpacing.marginMedium),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
