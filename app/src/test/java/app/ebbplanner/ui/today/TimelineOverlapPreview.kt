package app.ebbplanner.ui.today

import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.ebbplanner.ui.theme.EbbTheme
import androidx.compose.runtime.Composable

@Composable
fun TimelineOverlapPreview(state: TodayPresentationState) {
    EbbTheme {
        TodayTimeline(
            state = state.copy(showNowLine = false),
            modifier = Modifier.height(720.dp),
            onTaskClick = {},
        )
    }
}