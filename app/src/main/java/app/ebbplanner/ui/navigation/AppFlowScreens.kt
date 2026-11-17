@file:Suppress("NewLineAtEndOfFile")

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
import app.ebbplanner.ui.daytools.OverlapHelperDesign
import app.ebbplanner.ui.daytools.PlanMyDayDesign
import app.ebbplanner.ui.daytools.ShiftDayViewModel
import app.ebbplanner.ui.daytools.ShiftTheRestDesign
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
    val background by viewModel.backgroundState.collectAsStateWithLifecycle()
    FlowTheme {
        ShiftTheRestDesign(
            modifier = modifier.fillMaxSize(),
            shiftMinutes = viewModel.shiftMinutes,
            backgroundState = background,
            onApplyShift = { viewModel.applyShift { navController.popBackStack() } },
            onCancel = { navController.popBackStack() },
        )
    }
}

@Suppress("UnusedParameter")
@Composable
fun PlanDayScreen(
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
) {
    FlowTheme {
        PlanMyDayDesign(modifier = modifier.fillMaxSize())
    }
}

@Suppress("UnusedParameter")
@Composable
fun OverlapHelperScreen(
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
) {
    FlowTheme {
        OverlapHelperDesign(modifier = modifier.fillMaxSize())
    }
}

@Suppress("UnusedParameter")
@Composable
fun MakeItFitScreen(
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
) {
    FlowTheme {
        MakeItFitDesign(modifier = modifier.fillMaxSize())
    }
}

@Composable
fun FocusRunScreen(
    modifier: Modifier = Modifier,
    viewModel: FocusRunViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    FlowTheme {
        FocusScreenDesign(
            state = state,
            modifier = modifier.fillMaxSize(),
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
                    onStartFocus =
                        {
                            if (state.mode != TaskDetailMode.NotFound) {
                                navController.navigate(AppRoutes.FOCUS_RUN)
                            }
                        },
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