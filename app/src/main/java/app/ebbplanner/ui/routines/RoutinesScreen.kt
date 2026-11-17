@file:Suppress("NewLineAtEndOfFile")

package app.ebbplanner.ui.routines

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun RoutinesScreen(
    modifier: Modifier = Modifier,
    viewModel: RoutinesViewModel = hiltViewModel(),
    onCreateRoutine: (String) -> Unit = {},
) {
    val state by viewModel.presentation.collectAsStateWithLifecycle()
    RoutinesScreenDesign(
        state = state,
        modifier = modifier,
        onFabClick = { viewModel.openNewEditor(onCreateRoutine) },
        onRoutineClick = onCreateRoutine,
    )
}
