package app.ebbplanner.ui.inbox

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun InboxScreen(modifier: Modifier = Modifier) {
    val presentation = remember { inboxPopulated() }
    InboxScreenDesign(state = presentation, modifier = modifier)
}
