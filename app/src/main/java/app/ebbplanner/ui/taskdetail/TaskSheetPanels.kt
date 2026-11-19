@file:Suppress("LongMethod", "MagicNumber", "MaxLineLength", "LongParameterList")

package app.ebbplanner.ui.taskdetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ebbplanner.R
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.ui.components.EbbCancelButton
import app.ebbplanner.ui.components.EbbConfirmButton
import app.ebbplanner.ui.components.EbbSheetActions
import app.ebbplanner.ui.theme.EbbShapes
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbSymbol
import app.ebbplanner.ui.theme.EbbTaskColors
import app.ebbplanner.ui.theme.EbbTypography
import app.ebbplanner.ui.time.ClockFormat
import java.time.Instant
import java.time.LocalDate

const val SCHEDULE_DATE_CHIP_TAG = "schedule_date_chip"
const val SCHEDULE_TIME_CHIP_TAG = "schedule_time_chip"

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
                    onCancel = onDismiss,
                )
            TaskSheetType.Duration ->
                DurationSheetPanel(
                    selectedMin = state.durationMin,
                    onSave = {
                        onSaveDuration(it)
                        onDismiss()
                    },
                    onCancel = onDismiss,
                )
            TaskSheetType.BreakDown ->
                BreakDownSheetPanel(
                    taskTitle = state.title,
                    existing = state.subtasks,
                    onAdd = {
                        onAddSubtask(it)
                        onDismiss()
                    },
                    onCancel = onDismiss,
                )
            TaskSheetType.Schedule ->
                ScheduleSheetPanel(
                    taskTitle = state.title,
                    suggestions = state.scheduleSuggestions,
                    existingDate = state.scheduledDate,
                    existingStartLocalTime = state.scheduledStartLocalTime,
                    onSave = { placement, date, time ->
                        onSaveSchedule(placement, date, time)
                        onDismiss()
                    },
                    onCancel = onDismiss,
                )
            TaskSheetType.ColorIcon ->
                ColorIconSheetPanel(
                    colorKey = state.colorKey,
                    iconKey = state.colorIconKey,
                    onSave = { color, icon ->
                        onSaveColorIcon(color, icon)
                        onDismiss()
                    },
                    onCancel = onDismiss,
                )
        }
    }
}

@Composable
private fun ReminderSheetPanel(
    enabled: Boolean,
    onSave: (Boolean) -> Unit,
    onCancel: () -> Unit = {},
) {
    var on by remember(enabled) { mutableStateOf(enabled) }
    Column(Modifier.padding(EbbSpacing.s4), verticalArrangement = Arrangement.spacedBy(EbbSpacing.s3)) {
        Text("Reminders", style = EbbTypography.titleL)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Remind at start", style = EbbTypography.bodyL)
            Switch(checked = on, onCheckedChange = { on = it })
        }
        EbbSheetActions(
            confirmLabel = stringResource(R.string.action_save),
            onConfirm = { onSave(on) },
            onCancel = onCancel,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DurationSheetPanel(
    selectedMin: Int,
    onSave: (Int) -> Unit,
    onCancel: () -> Unit = {},
) {
    var selected by remember(selectedMin) { mutableIntStateOf(selectedMin) }
    Column(Modifier.padding(EbbSpacing.s4)) {
        Text("How long might it take?", style = EbbTypography.titleL)
        FlowRow(
            Modifier.fillMaxWidth().padding(vertical = EbbSpacing.s3),
            horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
            verticalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
        ) {
            durationPresets.forEach { min ->
                val isSel = min == selected
                Surface(
                    onClick = { selected = min },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    border = if (isSel) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                ) {
                    Text(
                        "$min min",
                        style = EbbTypography.labelL,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        maxLines = 1,
                    )
                }
            }
        }
        EbbSheetActions(
            confirmLabel = stringResource(R.string.action_done),
            onConfirm = { onSave(selected) },
            onCancel = onCancel,
        )
    }
}

@Composable
private fun BreakDownSheetPanel(
    taskTitle: String,
    existing: List<TaskSubtaskRow>,
    onAdd: (String) -> Unit,
    onCancel: () -> Unit = {},
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
        EbbSheetActions(
            confirmLabel = stringResource(R.string.action_add_step),
            onConfirm = { if (draft.isNotBlank()) onAdd(draft.trim()) },
            onCancel = onCancel,
            confirmEnabled = draft.isNotBlank(),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun ScheduleSheetPanel(
    taskTitle: String,
    suggestions: List<ScheduleSuggestion>,
    existingDate: LocalDate? = null,
    existingStartLocalTime: String? = null,
    onSave: (String, LocalDate?, String?) -> Unit,
    onCancel: () -> Unit = {},
) {
    val fallbackMinutes =
        suggestions.firstOrNull { it.subtitle == "Next free slot" }
            ?.startLocalTime
            ?.let { ClockFormat.parseMinutes(it) }
            ?: (9 * 60)
    val initialMinutes = existingStartLocalTime?.let { ClockFormat.parseMinutes(it) } ?: fallbackMinutes
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var pickedDate by remember { mutableStateOf(existingDate ?: LocalDate.now()) }
    var pickedTimeMinutes by remember { mutableIntStateOf(initialMinutes) }
    val is24Hour = android.text.format.DateFormat.is24HourFormat(LocalContext.current)
    if (showDatePicker) {
        val dateState =
            rememberDatePickerState(
                initialSelectedDateMillis = pickedDate.atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli(),
            )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                EbbConfirmButton(
                    label = stringResource(R.string.action_save),
                    onClick = {
                        dateState.selectedDateMillis?.let { millis ->
                            pickedDate = Instant.ofEpochMilli(millis).atZone(java.time.ZoneOffset.UTC).toLocalDate()
                        }
                        showDatePicker = false
                    },
                )
            },
            dismissButton = {
                EbbCancelButton(
                    label = stringResource(R.string.action_cancel),
                    onClick = { showDatePicker = false },
                )
            },
        ) {
            DatePicker(state = dateState)
        }
    }
    if (showTimePicker) {
        val hour = pickedTimeMinutes / 60
        val minute = pickedTimeMinutes % 60
        val timeState = rememberTimePickerState(initialHour = hour, initialMinute = minute, is24Hour = is24Hour)
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                EbbConfirmButton(
                    label = stringResource(R.string.action_save),
                    onClick = {
                        pickedTimeMinutes = timeState.hour * 60 + timeState.minute
                        showTimePicker = false
                    },
                )
            },
            dismissButton = {
                EbbCancelButton(
                    label = stringResource(R.string.action_cancel),
                    onClick = { showTimePicker = false },
                )
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
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(top = EbbSpacing.s2),
            horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
            verticalArrangement = Arrangement.spacedBy(EbbSpacing.s1),
        ) {
            FilterChip(
                selected = true,
                onClick = { showDatePicker = true },
                modifier = Modifier.testTag(SCHEDULE_DATE_CHIP_TAG),
                label = {
                    Text(
                        pickedDate.format(java.time.format.DateTimeFormatter.ofPattern("d MMM")),
                        maxLines = 1,
                        style = EbbTypography.labelM,
                    )
                },
            )
            FilterChip(
                selected = true,
                onClick = { showTimePicker = true },
                modifier = Modifier.testTag(SCHEDULE_TIME_CHIP_TAG),
                label = {
                    Text(
                        ClockFormat.minutesToClockDisplay(pickedTimeMinutes, is24Hour),
                        maxLines = 1,
                        style = EbbTypography.labelM,
                    )
                },
            )
        }
        EbbSheetActions(
            confirmLabel = stringResource(R.string.action_use_selection),
            onConfirm = {
                onSave(
                    TaskEntity.PLACEMENT_SCHEDULED,
                    pickedDate,
                    ClockFormat.minutesToLocalTime(pickedTimeMinutes),
                )
            },
            onCancel = onCancel,
            modifier = Modifier.padding(top = EbbSpacing.s2),
        )
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorIconSheetPanel(
    colorKey: String,
    iconKey: String,
    onSave: (String, String) -> Unit,
    onCancel: () -> Unit = {},
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
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            iconOptions.forEach { key ->
                val isSel = icon == key
                Surface(
                    onClick = { icon = key },
                    shape = EbbShapes.md,
                    color =
                        if (isSel) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                    border =
                        if (isSel) {
                            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        } else {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        },
                ) {
                    EbbSymbol(
                        key,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(12.dp),
                        color =
                            if (isSel) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                    )
                }
            }
        }
        EbbSheetActions(
            confirmLabel = stringResource(R.string.action_save),
            onConfirm = { onSave(color, icon) },
            onCancel = onCancel,
        )
    }
}
