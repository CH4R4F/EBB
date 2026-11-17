@file:Suppress("NewLineAtEndOfFile")

package app.ebbplanner.ui.routines

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun RoutinesScreen(modifier: Modifier = Modifier) {
    RoutinesScreenDesign(state = routinesPopulated(), modifier = modifier)
}
