package app.ebbplanner.ui.week

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ebbplanner.ui.today.QuickCaptureSheet
import java.time.LocalDate

@Composable
fun WeekScreen(
    modifier: Modifier = Modifier,
    viewModel: WeekViewModel = hiltViewModel(),
) {
    val state by viewModel.presentation.collectAsStateWithLifecycle()
    var showCapture by rememberSaveable { mutableStateOf(false) }

    WeekScreenDesign(
        state = state,
        modifier = modifier,
        onPreviousWeek = viewModel::previousWeek,
        onNextWeek = viewModel::nextWeek,
        onThisWeek = viewModel::jumpToThisWeek,
        onFabClick = { showCapture = true },
        onDaySelected = viewModel::selectDay,
    )

    QuickCaptureSheet(
        visible = showCapture,
        defaultDate = state.selectedDate,
        onDismiss = { showCapture = false },
        onSave = viewModel::captureTask,
    )
}