@file:Suppress("LongMethod", "MagicNumber", "MaxLineLength", "LongParameterList")

package app.ebbplanner.ui.taskdetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.ui.theme.EbbShapes
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbTaskColors
import app.ebbplanner.ui.theme.EbbTypography
import androidx.compose.ui.res.stringResource
import app.ebbplanner.R
import app.ebbplanner.ui.time.ClockFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private val durationPresets = listOf(5, 10, 15, 30, 45, 60, 90)
private val colorOptions = listOf("slate", "lemon", "sky", "rose", "amber", "green")
private val iconOptions = listOf("task", "edit_note", "local_post_office", "call", "fitness_center", "shopping_cart")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskSheetHost(
    sheet: TaskSheetType?,
    state: TaskDetailPresentationState,
    onDismiss: () -> Unit,
    onSaveReminder: (Boolean) -> Unit,
    onSaveDuration: (Int) -> Unit,
    onAddSubtask: (String) -> Unit,
    onSaveSchedule: (String, LocalDate?, String?) -> Unit,
    onSaveColorIcon: (String, String) -> Unit,
) {
    if (sheet == null) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        when (sheet) {
            TaskSheetType.Reminder ->
                ReminderSheetPanel(
                    enabled = state.remindersEnabled,
                    onSave = {
                        onSaveReminder(it)
                        onDismiss()
                    },
                )
            TaskSheetType.Duration ->
                DurationSheetPanel(
                    selectedMin = state.durationMin,
                    onSave = {
                        onSaveDuration(it)
                        onDismiss()
                    },
                )
            TaskSheetType.BreakDown ->
                BreakDownSheetPanel(
                    taskTitle = state.title,
                    existing = state.subtasks,
                    onAdd = {
                        onAddSubtask(it)
                        onDismiss()
                    },
                )
            TaskSheetType.Schedule ->
                ScheduleSheetPanel(
                    taskTitle = state.title,
                    suggestions = state.scheduleSuggestions,
                    onSave = { placement, date, time ->
                        onSaveSchedule(placement, date, time)
                        onDismiss()
                    },
                )
            TaskSheetType.ColorIcon ->
                ColorIconSheetPanel(
                    colorKey = state.colorKey,
                    iconKey = state.colorIconKey,
                    onSave = { color, icon ->
                        onSaveColorIcon(color, icon)
                        onDismiss()
                    },
                )
        }
    }
}

@Composable
private fun ReminderSheetPanel(
    enabled: Boolean,
    onSave: (Boolean) -> Unit,
) {
    var on by remember(enabled) { mutableStateOf(enabled) }
    Column(Modifier.padding(EbbSpacing.s4), verticalArrangement = Arrangement.spacedBy(EbbSpacing.s3)) {
        Text("Reminders", style = EbbTypography.titleL)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Remind at start", style = EbbTypography.bodyL)
            Switch(checked = on, onCheckedChange = { on = it })
        }
        TextButton(onClick = { onSave(on) }, modifier = Modifier.align(Alignment.End)) {
            Text("Save", style = EbbTypography.labelL)
        }
    }
}

@Composable
private fun DurationSheetPanel(
    selectedMin: Int,
    onSave: (Int) -> Unit,
) {
    var selected by remember(selectedMin) { mutableIntStateOf(selectedMin) }
    Column(Modifier.padding(EbbSpacing.s4)) {
        Text("How long might it take?", style = EbbTypography.titleL)
        Row(Modifier.fillMaxWidth().padding(vertical = EbbSpacing.s3), horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2)) {
            durationPresets.forEach { min ->
                Text(
                    "$min m",
                    modifier =
                        Modifier
                            .clickable { selected = min }
                            .padding(8.dp),
                    style = EbbTypography.labelL,
                    color =
                        if (min == selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                )
            }
        }
        TextButton(onClick = { onSave(selected) }, modifier = Modifier.align(Alignment.End)) {
            Text("Done", style = EbbTypography.labelL)
        }
    }
}

@Composable
private fun BreakDownSheetPanel(
    taskTitle: String,
    existing: List<TaskSubtaskRow>,
    onAdd: (String) -> Unit,
) {
    var draft by remember { mutableStateOf("") }
    Column(Modifier.padding(EbbSpacing.s4), verticalArrangement = Arrangement.spacedBy(EbbSpacing.s2)) {
        Text("Break it down", style = EbbTypography.titleL)
        Text(taskTitle, style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
        existing.forEach { row ->
            Text("• ${row.title}", style = EbbTypography.bodyM)
        }
        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Tiny step") },
            singleLine = true,
        )
        TextButton(
            onClick = { if (draft.isNotBlank()) onAdd(draft.trim()) },
            enabled = draft.isNotBlank(),
            modifier = Modifier.align(Alignment.End),
        ) {
            Text("Add step", style = EbbTypography.labelL)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleSheetPanel(
    taskTitle: String,
    suggestions: List<ScheduleSuggestion>,
    onSave: (String, LocalDate?, String?) -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var pickedDate by remember { mutableStateOf(LocalDate.now()) }
    var pickedTimeMinutes by remember { mutableIntStateOf(9 * 60) }
    val zone = ZoneId.systemDefault()

    if (showDatePicker) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = pickedDate.atStartOfDay(zone).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        dateState.selectedDateMillis?.let { millis ->
                            pickedDate = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
                        }
                        showDatePicker = false
                    },
                ) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.onboarding_not_now))
                }
            },
        ) {
            DatePicker(state = dateState)
        }
    }
    if (showTimePicker) {
        val hour = pickedTimeMinutes / 60
        val minute = pickedTimeMinutes % 60
        val timeState = rememberTimePickerState(initialHour = hour, initialMinute = minute, is24Hour = true)
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickedTimeMinutes = timeState.hour * 60 + timeState.minute
                        showTimePicker = false
                    },
                ) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.onboarding_not_now))
                }
            },
            text = { TimePicker(state = timeState) },
        )
    }

    Column(Modifier.padding(EbbSpacing.s4), verticalArrangement = Arrangement.spacedBy(EbbSpacing.s2)) {
        Text("Schedule", style = EbbTypography.titleL)
        Text(taskTitle, style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
        suggestions.forEach { suggestion ->
            ScheduleChoice(
                label = suggestion.label,
                subtitle = suggestion.subtitle,
                onClick = {
                    onSave(suggestion.placement, suggestion.date, suggestion.startLocalTime)
                },
            )
        }
        Row(
            Modifier.fillMaxWidth().padding(top = EbbSpacing.s2),
            horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
        ) {
            TextButton(onClick = { showDatePicker = true }) {
                Text("Pick date", style = EbbTypography.labelL)
            }
            TextButton(onClick = { showTimePicker = true }) {
                Text("Pick time", style = EbbTypography.labelL)
            }
            TextButton(
                onClick = {
                    onSave(
                        TaskEntity.PLACEMENT_SCHEDULED,
                        pickedDate,
                        ClockFormat.minutesToLocalTime(pickedTimeMinutes),
                    )
                },
                modifier = Modifier.align(Alignment.CenterVertically),
            ) {
                Text("Use selection", style = EbbTypography.labelL)
            }
        }
    }
}

@Composable
private fun ScheduleChoice(
    label: String,
    subtitle: String? = null,
    onClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp),
    ) {
        Text(label, style = EbbTypography.bodyL)
        subtitle?.let {
            Text(it, style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ColorIconSheetPanel(
    colorKey: String,
    iconKey: String,
    onSave: (String, String) -> Unit,
) {
    var color by remember(colorKey) { mutableStateOf(colorKey) }
    var icon by remember(iconKey) { mutableStateOf(iconKey) }
    Column(Modifier.padding(EbbSpacing.s4)) {
        Text("Color & icon", style = EbbTypography.titleL)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
            colorOptions.forEach { key ->
                androidx.compose.material3.Surface(
                    onClick = { color = key },
                    shape = EbbShapes.md,
                    color = EbbTaskColors.tint(key, dark = true),
                    modifier = Modifier.padding(4.dp),
                    border =
                        if (color == key) {
                            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        } else {
                            null
                        },
                ) {
                    Text(" ", modifier = Modifier.padding(16.dp))
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            iconOptions.forEach { key ->
                Text(
                    key,
                    modifier = Modifier.clickable { icon = key }.padding(8.dp),
                    style = EbbTypography.labelM,
                    color =
                        if (icon == key) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                )
            }
        }
        TextButton(onClick = { onSave(color, icon) }, modifier = Modifier.align(Alignment.End)) {
            Text("Save", style = EbbTypography.labelL)
        }
    }
}
