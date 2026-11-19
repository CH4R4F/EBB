@file:Suppress("LongMethod")

package app.ebbplanner.ui.inbox

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ebbplanner.R
import app.ebbplanner.ui.today.QuickCaptureSaveTarget
import app.ebbplanner.ui.today.QuickCaptureSheet
import app.ebbplanner.ui.today.quickCaptureSavedMessage
import app.ebbplanner.ui.today.showQuickCaptureSavedSnackbar
import kotlinx.coroutines.launch
import java.time.LocalTime

@Composable
fun InboxScreen(
    modifier: Modifier = Modifier,
    viewModel: InboxViewModel = hiltViewModel(),
    onOpenTask: (String) -> Unit = {},
) {
    val presentation by viewModel.presentation.collectAsStateWithLifecycle()
    var showCapture by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val showLabel = stringResource(R.string.action_show)
    val resources = LocalContext.current.resources

    InboxScreenDesign(
        state = presentation,
        modifier = modifier.fillMaxSize(),
        snackbarHostState = snackbarHostState,
        onFabClick = { showCapture = true },
        onTaskClick = onOpenTask,
        onSearchToggle = viewModel::toggleSearch,
        onSearchQueryChange = viewModel::setSearchQuery,
        onFilterSelected = viewModel::setFilterIndex,
    )

    QuickCaptureSheet(
        visible = showCapture,
        onDismiss = { showCapture = false },
        onSave = { draft ->
            viewModel.captureToInbox(draft) { task, resolved ->
                scope.launch {
                    val result =
                        snackbarHostState.showQuickCaptureSavedSnackbar(
                            message = quickCaptureSavedMessage(resources, resolved),
                            actionLabel = showLabel,
                        )
                    if (result == SnackbarResult.ActionPerformed) {
                        onOpenTask(task.id)
                    }
                }
            }
        },
        saveTarget = QuickCaptureSaveTarget.Inbox,
        nowMinutes = LocalTime.now().hour * 60 + LocalTime.now().minute,
    )
}
