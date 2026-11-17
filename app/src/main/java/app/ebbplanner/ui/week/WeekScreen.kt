package app.ebbplanner.ui.week

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun WeekScreen(modifier: Modifier = Modifier) {
    WeekScreenDesign(state = weekPortrait(), modifier = modifier)
}
