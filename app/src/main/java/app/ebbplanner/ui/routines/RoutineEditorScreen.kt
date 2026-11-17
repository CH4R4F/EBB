package app.ebbplanner.ui.routines

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ebbplanner.data.local.entity.RoutineEntity
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbTypography

@Composable
fun RoutineEditorScreen(
    onClose: () -> Unit,
    viewModel: RoutineEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().padding(EbbSpacing.s4)) {
        Text("Edit routine", style = EbbTypography.headlineM)
        OutlinedTextField(
            value = state.title,
            onValueChange = viewModel::setTitle,
            modifier = Modifier.fillMaxWidth().padding(vertical = EbbSpacing.s2),
            label = { Text("Name") },
            singleLine = true,
        )
        Text("Repeat", style = EbbTypography.labelL, modifier = Modifier.padding(top = EbbSpacing.s2))
        listOf(
            RoutineEntity.REPEAT_DAILY to "Daily",
            RoutineEntity.REPEAT_WEEKDAYS to "Weekdays",
            RoutineEntity.REPEAT_WEEKLY to "Weekly",
        ).forEach { (rule, label) ->
            TextButton(onClick = { viewModel.setRepeatRule(rule) }) {
                Text(
                    label,
                    color =
                        if (state.repeatRule == rule) {
                            androidx.compose.material3.MaterialTheme.colorScheme.primary
                        } else {
                            androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                        },
                )
            }
        }
        OutlinedTextField(
            value = state.startLocalTime,
            onValueChange = viewModel::setStartTime,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Start time (HH:mm)") },
            singleLine = true,
        )
        OutlinedTextField(
            value = state.durationMin.toString(),
            onValueChange = { value -> value.toIntOrNull()?.let(viewModel::setDuration) },
            modifier = Modifier.fillMaxWidth().padding(top = EbbSpacing.s2),
            label = { Text("Duration (minutes)") },
            singleLine = true,
        )
        TextButton(onClick = { viewModel.save(onClose) }, modifier = Modifier.padding(top = EbbSpacing.s4)) {
            Text("Save", style = EbbTypography.labelL)
        }
        TextButton(onClick = onClose) {
            Text("Cancel")
        }
    }
}
