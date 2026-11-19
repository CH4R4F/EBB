@file:Suppress("LongMethod", "LongParameterList")

package app.ebbplanner.ui.today

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarDuration
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ebbplanner.R
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

const val TODAY_DATE_HEADER_TAG = "today_date_header"
const val TODAY_TIMELINE_TAG = "today_timeline"
const val TIMELINE_TASK_TAG_PREFIX = "timeline_task_"
const val QUICK_CAPTURE_SNACKBAR_HOST_TAG = "quick_capture_snackbar_host"
const val QUICK_CAPTURE_FAB_TAG = "quick_capture_fab"

suspend fun SnackbarHostState.showQuickCaptureSavedSnackbar(
    message: String,
    actionLabel: String,
): SnackbarResult {
    currentSnackbarData?.dismiss()
    return showSnackbar(
        message = message,
        actionLabel = actionLabel,
        duration = SnackbarDuration.Long,
    )
}

class QuickCaptureSaveNotifier {
    var count: Int = 0
        private set
    var lastTaskId: String? = null
        private set

    fun record(taskId: String) {
        count += 1
        lastTaskId = taskId
    }

    fun message(
        resources: android.content.res.Resources,
        singleMessage: String,
    ): String =
        if (count <= 1) {
            singleMessage
        } else {
            resources.getQuantityString(R.plurals.quick_capture_saved_count, count, count)
        }

    fun resetIfCurrent(expectedCount: Int) {
        if (count == expectedCount) {
            count = 0
            lastTaskId = null
        }
    }
}

fun timelineTaskTestTag(taskId: String): String = TIMELINE_TASK_TAG_PREFIX + taskId

@Suppress("LongParameterList")
@Composable
fun TodayScreen(
    modifier: Modifier = Modifier,
    viewModel: TodayViewModel = hiltViewModel(),
    onOpenPlanDay: () -> Unit = {},
    onOpenShiftDay: () -> Unit = {},
    onOpenOverlap: () -> Unit = {},
    onOpenMakeItFit: () -> Unit = {},
    onOpenTask: (String) -> Unit = {},
    onOpenTaskSchedule: (String) -> Unit = {},
    onOpenSettings: () -> Unit = {},
) {
    val presentation by viewModel.presentationState.collectAsStateWithLifecycle()

    var showCapture by rememberSaveable { mutableStateOf(false) }
    var capturePrefill by rememberSaveable { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val saveNotifier = remember { QuickCaptureSaveNotifier() }
    val scope = rememberCoroutineScope()
    val showLabel = stringResource(R.string.action_show)
    val resources = LocalContext.current.resources
    val wallNowMinutes = LocalTime.now().hour * 60 + LocalTime.now().minute
    val viewedToday = presentation.date == LocalDate.now()
    val nowMinutes = presentation.nowMinutes ?: if (viewedToday) wallNowMinutes else 0
    val occupied =
        presentation.blocks.filter { !it.done }.map { it.startMinutes to it.durationMin } +
            presentation.events.map { it.startMinutes to it.durationMin }

    TodayScreenDesign(
        state = presentation,
        modifier = modifier.fillMaxSize().testTag(TODAY_TIMELINE_TAG),
        showBottomNav = false,
        snackbarHostState = snackbarHostState,
        onFabClick = {
            capturePrefill = ""
            showCapture = true
        },
        onPlanDayClick = onOpenPlanDay,
        onAddTaskClick = {
            capturePrefill = ""
            showCapture = true
        },
        onShiftClick = onOpenShiftDay,
        onOpenOverlap = onOpenOverlap,
        onOpenMakeItFit = onOpenMakeItFit,
        onPrevDay = viewModel::goToPreviousDay,
        onNextDay = viewModel::goToNextDay,
        onSettings = onOpenSettings,
        onAnytimeClick = onOpenTask,
        onAnytimeDone = viewModel::markTaskDone,
        onAddAnytimeCapture = {
            capturePrefill = ""
            showCapture = true
        },
        onTaskClick = onOpenTask,
        onTaskLongPress = onOpenTaskSchedule,
        onMarkTaskDone = viewModel::markTaskDone,
    )

    QuickCaptureSheet(
        visible = showCapture,
        onDismiss = { showCapture = false },
        onSave = { draft ->
            viewModel.captureTask(draft) { task, resolved ->
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
        defaultDate = presentation.date,
        defaultText = capturePrefill,
        nowMinutes = nowMinutes,
        occupied = occupied,
        occupiedTomorrow = presentation.tomorrowOccupied,
        windowStart = presentation.wakeMinutes,
        windowEnd = presentation.bedMinutes,
    )
}

@Composable
fun TodayScreenContent(
    state: TodayUiState,
    modifier: Modifier = Modifier,
) {
    val fontScale = androidx.compose.ui.platform.LocalConfiguration.current.fontScale
    val is24Hour = app.ebbplanner.ui.time.ClockFormat.isSystem24Hour(LocalContext.current)
    val presentation = TodayPresentationMapper.from(state.copy(is24Hour = is24Hour), fontScale = fontScale)
    TodayScreenDesign(state = presentation, modifier = modifier, showBottomNav = false)
}
