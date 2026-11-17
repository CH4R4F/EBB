package app.ebbplanner.ui.week

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun WeekScreen(
    modifier: Modifier = Modifier,
    viewModel: WeekViewModel = hiltViewModel(),
) {
    val state by viewModel.presentation.collectAsStateWithLifecycle()
    WeekScreenDesign(
        state = state,
        modifier = modifier,
        onPreviousWeek = viewModel::previousWeek,
        onNextWeek = viewModel::nextWeek,
    )
}
