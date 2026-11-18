package app.ebbplanner.ui.inbox

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ebbplanner.ui.today.QuickCaptureSaveTarget
import app.ebbplanner.ui.today.QuickCaptureSheet

@Composable
fun InboxScreen(
    modifier: Modifier = Modifier,
    viewModel: InboxViewModel = hiltViewModel(),
    onOpenTask: (String) -> Unit = {},
) {
    val presentation by viewModel.presentation.collectAsStateWithLifecycle()
    var showCapture by rememberSaveable { mutableStateOf(false) }

    InboxScreenDesign(
        state = presentation,
        modifier = modifier,
        onFabClick = { showCapture = true },
        onTaskClick = onOpenTask,
        onSearchToggle = viewModel::toggleSearch,
        onSearchQueryChange = viewModel::setSearchQuery,
        onFilterSelected = viewModel::setFilterIndex,
    )

    QuickCaptureSheet(
        visible = showCapture,
        onDismiss = { showCapture = false },
        onSave = viewModel::captureToInbox,
        saveTarget = QuickCaptureSaveTarget.Inbox,
    )
}