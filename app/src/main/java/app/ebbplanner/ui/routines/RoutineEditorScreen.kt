package app.ebbplanner.ui.routines

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ebbplanner.R
import app.ebbplanner.data.local.entity.RoutineEntity
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbTypography
import app.ebbplanner.ui.time.ClockFormat

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RoutineEditorScreen(
    onClose: () -> Unit,
    viewModel: RoutineEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showTimePicker by remember { mutableStateOf(false) }
    val timeParts = remember(state.startLocalTime) { ClockFormat.parseMinutes(state.startLocalTime) ?: (9 * 60) }
    val pickerState =
        rememberTimePickerState(
            initialHour = timeParts / 60,
            initialMinute = timeParts % 60,
            is24Hour = true,
        )

    Column(Modifier.fillMaxSize().padding(EbbSpacing.s4)) {
        Text(
            if (state.isNew) stringResource(R.string.routine_editor_new) else state.title,
            style = EbbTypography.headlineM,
        )
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
        if (state.repeatRule == RoutineEntity.REPEAT_WEEKLY) {
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = EbbSpacing.s1),
                horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
            ) {
                listOf("Mon" to 1, "Tue" to 2, "Wed" to 3, "Thu" to 4, "Fri" to 5, "Sat" to 6, "Sun" to 7).forEach { (label, day) ->
                    FilterChip(
                        selected = state.weeklyDays.contains(day),
                        onClick = { viewModel.toggleWeeklyDay(day) },
                        label = { Text(label) },
                    )
                }
            }
        }
        TextButton(onClick = { showTimePicker = true }) {
            Text("Start time · ${state.startLocalTime}", style = EbbTypography.bodyL)
        }
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
        if (!state.isNew) {
            TextButton(onClick = { viewModel.delete(onClose) }) {
                Text(stringResource(R.string.routine_delete))
            }
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val minutes = pickerState.hour * 60 + pickerState.minute
                        viewModel.setStartTime(ClockFormat.minutesToLocalTime(minutes))
                        showTimePicker = false
                    },
                ) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Start time") },
            text = { TimePicker(state = pickerState) },
        )
    }
}