package app.ebbplanner.ui.today

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

const val TODAY_DATE_HEADER_TAG = "today_date_header"
const val TODAY_TIMELINE_TAG = "today_timeline"

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

    TodayScreenDesign(
        state = presentation,
        modifier = modifier.fillMaxSize().testTag(TODAY_TIMELINE_TAG),
        showBottomNav = false,
        onFabClick = { showCapture = true },
        onPlanDayClick = onOpenPlanDay,
        onAddTaskClick = { showCapture = true },
        onShiftClick = onOpenShiftDay,
        onOpenOverlap = onOpenOverlap,
        onOpenMakeItFit = onOpenMakeItFit,
        onPrevDay = viewModel::goToPreviousDay,
        onNextDay = viewModel::goToNextDay,
        onSettings = onOpenSettings,
        onAnytimeClick = onOpenTask,
        onAnytimeDone = viewModel::markTaskDone,
        onTaskClick = onOpenTask,
        onTaskLongPress = onOpenTaskSchedule,
        onMarkTaskDone = viewModel::markTaskDone,
    )

    QuickCaptureSheet(
        visible = showCapture,
        onDismiss = { showCapture = false },
        onSave = viewModel::captureTask,
    )
}

@Composable
fun TodayScreenContent(
    state: TodayUiState,
    modifier: Modifier = Modifier,
) {
    val presentation = TodayPresentationMapper.from(state)
    TodayScreenDesign(state = presentation, modifier = modifier, showBottomNav = false)
}
