package app.ebbplanner.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.ebbplanner.ui.daytools.PlanMyDayDesign
import app.ebbplanner.ui.daytools.ShiftTheRestDesign
import app.ebbplanner.ui.focus.FocusPresentationState
import app.ebbplanner.ui.focus.FocusScreenDesign
import app.ebbplanner.ui.focus.FocusVisualMode
import app.ebbplanner.ui.onboarding.OnboardingAppPreview
import app.ebbplanner.ui.taskdetail.TaskDetailScreenDesign
import app.ebbplanner.ui.taskdetail.TaskDetailViewModel

@Composable
fun ShiftDayScreen(modifier: Modifier = Modifier) {
    OnboardingAppPreview(darkTheme = true) {
        ShiftTheRestDesign(modifier = modifier.fillMaxSize())
    }
}

@Composable
fun PlanDayScreen(modifier: Modifier = Modifier) {
    OnboardingAppPreview(darkTheme = true) {
        PlanMyDayDesign(modifier = modifier.fillMaxSize())
    }
}

@Composable
fun FocusRunScreen(modifier: Modifier = Modifier) {
    OnboardingAppPreview(darkTheme = true) {
        FocusScreenDesign(
            state = FocusPresentationState(mode = FocusVisualMode.Setup),
            modifier = modifier.fillMaxSize(),
        )
    }
}

@Composable
fun TaskDetailRoute(
    taskId: String,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: TaskDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    androidx.compose.runtime.LaunchedEffect(taskId) {
        viewModel.load(taskId)
    }
    OnboardingAppPreview(darkTheme = true) {
        TaskDetailScreenDesign(
            state = state,
            modifier = modifier.fillMaxSize(),
            onClose = { navController.popBackStack() },
            onMarkDone = viewModel::markDone,
            onStartFocus = { navController.navigate(AppRoutes.FOCUS_RUN) },
        )
    }
}
