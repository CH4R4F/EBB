package app.ebbplanner.ui.inbox

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun InboxScreen(
    modifier: Modifier = Modifier,
    viewModel: InboxViewModel = hiltViewModel(),
) {
    val presentation by viewModel.presentation.collectAsStateWithLifecycle()
    InboxScreenDesign(state = presentation, modifier = modifier)
}
