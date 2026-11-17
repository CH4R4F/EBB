package app.ebbplanner.ui.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.ebbplanner.ui.daytools.FocusRunViewModel
import app.ebbplanner.ui.daytools.MakeItFitDesign
import app.ebbplanner.ui.daytools.MakeItFitViewModel
import app.ebbplanner.ui.daytools.OverlapHelperDesign
import app.ebbplanner.ui.daytools.OverlapHelperViewModel
import app.ebbplanner.ui.daytools.PlanDayViewModel
import app.ebbplanner.ui.daytools.PlanMyDayDesign
import app.ebbplanner.ui.daytools.ShiftDayViewModel
import app.ebbplanner.ui.daytools.ShiftTheRestDesign
import app.ebbplanner.ui.focus.FocusCallbacks
import app.ebbplanner.ui.focus.FocusScreenDesign
import app.ebbplanner.ui.onboarding.OnboardingAppPreview
import app.ebbplanner.ui.taskdetail.TaskDetailCallbacks
import app.ebbplanner.ui.taskdetail.TaskDetailMode
import app.ebbplanner.ui.taskdetail.TaskDetailScreenDesign
import app.ebbplanner.ui.taskdetail.TaskDetailViewModel
import app.ebbplanner.ui.taskdetail.TaskSheetHost
import app.ebbplanner.ui.taskdetail.TaskSheetType

@Composable
private fun FlowTheme(content: @Composable () -> Unit) {
    OnboardingAppPreview(darkTheme = isSystemInDarkTheme(), content = content)
}

@Composable
fun ShiftDayScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: ShiftDayViewModel = hiltViewModel(),
) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    FlowTheme {
        ShiftTheRestDesign(
            modifier = modifier.fillMaxSize(),
            shiftMinutes = ui.shiftMinutes,
            chipIndex = ui.chipIndex,
            previewBefore = ui.previewBefore,
            previewAfter = ui.previewAfter,
            backgroundState = ui.background,
            onChipSelected = viewModel::selectChip,
            onApplyShift = { viewModel.applyShift { navController.popBackStack() } },
            onCancel = { navController.popBackStack() },
        )
    }
}

@Composable
fun PlanDayScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: PlanDayViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    FlowTheme {
        PlanMyDayDesign(
            modifier = modifier.fillMaxSize(),
            tasks = state.tasks,
            emptyCandidates = !state.hasCandidates,
            onClose = { navController.popBackStack() },
            onToggleTask = viewModel::toggleTask,
            onApply = { viewModel.applyPlan { navController.popBackStack() } },
        )
    }
}

@Composable
fun OverlapHelperScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: OverlapHelperViewModel = hiltViewModel(),
) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    FlowTheme {
        OverlapHelperDesign(
            modifier = modifier.fillMaxSize(),
            situation = ui.situation,
            selected = ui.selected,
            onSelectOption = viewModel::selectOption,
            onApply = { viewModel.apply { navController.popBackStack() } },
            onCancel = { navController.popBackStack() },
        )
    }
}

@Composable
fun MakeItFitScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: MakeItFitViewModel = hiltViewModel(),
) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    FlowTheme {
        MakeItFitDesign(
            modifier = modifier.fillMaxSize(),
            plan = ui.plan,
            selectedMoveIds = ui.selectedMoveIds,
            onToggleMove = viewModel::toggleMove,
            onApply = { viewModel.applyMoves { navController.popBackStack() } },
            onCancel = { navController.popBackStack() },
        )
    }
}

@Composable
fun FocusRunScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: FocusRunViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    FlowTheme {
        FocusScreenDesign(
            state = state,
            modifier = modifier.fillMaxSize(),
            callbacks =
                FocusCallbacks(
                    onClose = {
                        viewModel.close()
                        navController.popBackStack()
                    },
                    onStartFocus = viewModel::startFocus,
                    onJustFiveMinutes = viewModel::justFiveMinutes,
                    onPause = viewModel::pause,
                    onResume = viewModel::resume,
                    onAddFive = viewModel::addFive,
                    onDone = viewModel::markDone,
                    onDistracted = viewModel::distracted,
                    onTimesUpDone = viewModel::markDone,
                    onTimesUpAddFive = viewModel::timesUpAddFive,
                    onTimesUpAddFifteen = viewModel::timesUpAddFifteen,
                    onTimesUpShiftDay = {
                        viewModel.timesUpShiftDay()
                        navController.popBackStack()
                    },
                    onTimesUpBreak = viewModel::takeBreak,
                    onMenu = {},
                ),
        )
    }
}

@Composable
fun TaskDetailRoute(
    taskId: String,
    initialSheet: String,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: TaskDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val sheet by viewModel.openSheet.collectAsStateWithLifecycle()
    androidx.compose.runtime.LaunchedEffect(taskId) {
        viewModel.load(taskId)
    }
    androidx.compose.runtime.LaunchedEffect(initialSheet) {
        viewModel.openSheetFromRoute(initialSheet)
    }
    FlowTheme {
        TaskDetailScreenDesign(
            state = state,
            modifier = modifier.fillMaxSize(),
            callbacks =
                TaskDetailCallbacks(
                    onClose = { navController.popBackStack() },
                    onMarkDone = viewModel::markDone,
                    onStartFocus = { navController.navigate(AppRoutes.focusRun(taskId)) },
                    onOpenReminders = { viewModel.openSheet(TaskSheetType.Reminder) },
                    onOpenDuration = { viewModel.openSheet(TaskSheetType.Duration) },
                    onOpenBreakDown = { viewModel.openSheet(TaskSheetType.BreakDown) },
                    onOpenSchedule = { viewModel.openSheet(TaskSheetType.Schedule) },
                    onOpenColorIcon = { viewModel.openSheet(TaskSheetType.ColorIcon) },
                ),
        )
        if (state.mode != TaskDetailMode.NotFound && state.mode != TaskDetailMode.Loading) {
            TaskSheetHost(
                sheet = sheet,
                state = state,
                onDismiss = viewModel::dismissSheet,
                onSaveReminder = viewModel::saveReminder,
                onSaveDuration = viewModel::saveDuration,
                onAddSubtask = viewModel::addSubtask,
                onSaveSchedule = viewModel::saveSchedule,
                onSaveColorIcon = viewModel::saveColorIcon,
            )
        }
    }
}
