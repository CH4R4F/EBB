@file:Suppress("LongMethod")

package app.ebbplanner.ui.week

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
fun WeekScreen(
    modifier: Modifier = Modifier,
    viewModel: WeekViewModel = hiltViewModel(),
    onOpenTask: (String) -> Unit = {},
) {
    val state by viewModel.presentation.collectAsStateWithLifecycle()
    var showCapture by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val showLabel = stringResource(R.string.action_show)
    val resources = LocalContext.current.resources

    WeekScreenDesign(
        state = state,
        modifier = modifier.fillMaxSize(),
        snackbarHostState = snackbarHostState,
        onPreviousWeek = viewModel::previousWeek,
        onNextWeek = viewModel::nextWeek,
        onThisWeek = viewModel::jumpToThisWeek,
        onFabClick = { showCapture = true },
        onDaySelected = viewModel::selectDay,
    )

    QuickCaptureSheet(
        visible = showCapture,
        defaultDate = state.selectedDate,
        saveTarget = QuickCaptureSaveTarget.Week,
        onDismiss = { showCapture = false },
        onSave = { draft ->
            viewModel.captureTask(draft) { task, resolved ->
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
        nowMinutes = LocalTime.now().hour * 60 + LocalTime.now().minute,
    )
}
