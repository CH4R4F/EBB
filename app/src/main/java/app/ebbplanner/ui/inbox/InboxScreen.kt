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
import app.ebbplanner.ui.today.QuickCaptureSaveNotifier
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
    val saveNotifier = remember { QuickCaptureSaveNotifier() }
    val scope = rememberCoroutineScope()
    val showLabel = stringResource(R.string.action_show)
    val resources = LocalContext.current.resources

    val multiselect = presentation.visualMode == InboxVisualMode.Multiselect
    InboxScreenDesign(
        state = presentation,
        modifier = modifier.fillMaxSize(),
        snackbarHostState = snackbarHostState,
        onFabClick = { showCapture = true },
        onTaskClick = { id ->
            if (multiselect) viewModel.toggleSelected(id) else onOpenTask(id)
        },
        onTaskLongPress = viewModel::enterMultiselect,
        onSearchToggle = viewModel::toggleSearch,
        onSearchQueryChange = viewModel::setSearchQuery,
        onFilterSelected = viewModel::setFilterIndex,
        onMultiselectDone = viewModel::clearMultiselect,
        onSelectAll = viewModel::selectAllVisible,
        onMoveToToday = viewModel::moveSelectedToToday,
        onMoveToTomorrow = viewModel::moveSelectedToTomorrow,
        onLetGo = viewModel::letGoSelected,
        onSetDuration = viewModel::setSelectedDuration,
        onSetEnergy = viewModel::setSelectedEnergy,
    )

    QuickCaptureSheet(
        visible = showCapture,
        onDismiss = { showCapture = false },
        onSave = { draft ->
            viewModel.captureToInbox(draft) { task, resolved ->
                saveNotifier.record(task.id)
                val countAtShow = saveNotifier.count
                val message =
                    saveNotifier.message(resources, quickCaptureSavedMessage(resources, resolved))
                scope.launch {
                    val result =
                        snackbarHostState.showQuickCaptureSavedSnackbar(
                            message = message,
                            actionLabel = showLabel,
                        )
                    val taskId = saveNotifier.lastTaskId
                    saveNotifier.resetIfCurrent(countAtShow)
                    if (result == SnackbarResult.ActionPerformed && taskId != null) {
                        onOpenTask(taskId)
                    }
                }
            }
        },
        saveTarget = QuickCaptureSaveTarget.Inbox,
        nowMinutes = LocalTime.now().hour * 60 + LocalTime.now().minute,
    )
}
