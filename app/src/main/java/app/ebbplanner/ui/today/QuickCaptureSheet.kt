@file:Suppress("LongMethod", "LongParameterList")

package app.ebbplanner.ui.today

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.ebbplanner.R
import app.ebbplanner.domain.capture.QuickCaptureDraft
import app.ebbplanner.domain.capture.formatClockHm
import app.ebbplanner.domain.capture.nextFreeSlotMinutes
import app.ebbplanner.domain.capture.resolveQuickCapture
import app.ebbplanner.ui.components.EbbCancelButton
import app.ebbplanner.ui.components.EbbConfirmButton
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbTypography
import java.time.LocalDate
import java.time.LocalTime

enum class QuickCaptureSaveTarget {
    Today,
    Inbox,
    Week,
}

const val QUICK_CAPTURE_TEXT_FIELD_TAG = "quick_capture_text_field"
const val QUICK_CAPTURE_ADD_BUTTON_TAG = "quick_capture_add_button"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickCaptureSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onSave: (QuickCaptureDraft) -> Unit,
    defaultDate: LocalDate = LocalDate.now(),
    defaultText: String = "",
    saveTarget: QuickCaptureSaveTarget = QuickCaptureSaveTarget.Today,
    nowMinutes: Int? = null,
    occupied: List<Pair<Int, Int>> = emptyList(),
) {
    if (!visible) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        QuickCaptureEditor(
            defaultDate = defaultDate,
            defaultText = defaultText,
            saveTarget = saveTarget,
            nowMinutes = nowMinutes,
            occupied = occupied,
            onSave = onSave,
            onCancel = onDismiss,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickCaptureEditor(
    onSave: (QuickCaptureDraft) -> Unit,
    onCancel: () -> Unit,
    defaultDate: LocalDate = LocalDate.now(),
    defaultText: String = "",
    saveTarget: QuickCaptureSaveTarget = QuickCaptureSaveTarget.Today,
    nowMinutes: Int? = null,
    occupied: List<Pair<Int, Int>> = emptyList(),
) {
    var title by remember(defaultText) { mutableStateOf(defaultText) }
    var startOverride by remember { mutableStateOf<LocalTime?>(null) }
    var startOverridden by remember { mutableStateOf(false) }
    var durationOverride by remember { mutableStateOf<Int?>(null) }
    var durationOverridden by remember { mutableStateOf(false) }
    var nextFreeActive by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showCustomDuration by remember { mutableStateOf(false) }

    val draft =
        QuickCaptureDraft(
            raw = title,
            startOverride = startOverride,
            durationOverride = durationOverride,
            startOverridden = startOverridden,
            durationOverridden = durationOverridden,
        )
    val resolved = remember(draft, defaultDate, saveTarget) { resolveQuickCapture(draft, defaultDate, saveTarget) }
    val calendarToday = LocalDate.now()
    val preview = quickCapturePreviewLabel(resolved, calendarToday)
    val destination = quickCaptureDestinationLabel(resolved, calendarToday)
    val startLabel = resolved.startTime?.let { formatClockHm(it) }
    val showShortcuts = saveTarget == QuickCaptureSaveTarget.Today && resolved.startTime == null
    val resolvedNowMinutes = nowMinutes ?: (LocalTime.now().hour * 60 + LocalTime.now().minute)

    val applyNextFree: (Int) -> Unit = { durationMin ->
        val minutes = nextFreeSlotMinutes(resolvedNowMinutes, occupied, durationMin)
        startOverride = LocalTime.of((minutes / 60).coerceIn(0, 23), minutes % 60)
        startOverridden = true
        nextFreeActive = true
    }

    val persistDraft: (Boolean) -> Unit = { dismiss ->
        if (title.isNotBlank()) {
            onSave(draft)
            title = ""
            startOverride = null
            startOverridden = false
            durationOverride = null
            durationOverridden = false
            nextFreeActive = false
            if (dismiss) onCancel()
        }
    }

    Box(Modifier.fillMaxWidth()) {
        QuickCaptureSheetContent(
            state =
                QuickCapturePresentationState(
                    mode = if (title.isBlank()) QuickCaptureMode.Empty else QuickCaptureMode.Typing,
                    text = title,
                    highlights = app.ebbplanner.domain.capture.QuickCaptureParser.parse(title, defaultDate).highlights,
                    previewLabel = preview,
                    destinationLabel = destination,
                    startTimeLabel = startLabel,
                    selectedDurationMin = if (resolved.explicitDuration) resolved.durationMin else null,
                    showTodayShortcuts = showShortcuts,
                    saveEnabled = title.isNotBlank(),
                ),
            modifier = Modifier.fillMaxWidth(),
            showDragHandle = false,
            interactive = true,
            text = title,
            onTextChange = { title = it },
            onAdd = { persistDraft(true) },
            onImeDone = { persistDraft(false) },
            onCancel = onCancel,
            onPickStart = { showTimePicker = true },
            onClearStart = {
                startOverride = null
                startOverridden = true
                nextFreeActive = false
            },
            onNow = {
                startOverride = LocalTime.of((resolvedNowMinutes / 60).coerceIn(0, 23), resolvedNowMinutes % 60)
                startOverridden = true
                nextFreeActive = false
            },
            onNextFree = { applyNextFree(resolved.durationMin) },
            onSelectDuration = { minutes ->
                durationOverride = minutes
                durationOverridden = true
                if (nextFreeActive) applyNextFree(minutes)
            },
            onCustomDuration = { showCustomDuration = true },
        )

        if (showTimePicker) {
            val initial = resolved.startTime ?: LocalTime.now()
            val pickerState =
                rememberTimePickerState(
                    initialHour = initial.hour,
                    initialMinute = initial.minute,
                    is24Hour = true,
                )
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    EbbConfirmButton(
                        label = stringResource(R.string.action_save),
                        onClick = {
                            startOverride = LocalTime.of(pickerState.hour, pickerState.minute)
                            startOverridden = true
                            nextFreeActive = false
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
                title = { Text(stringResource(R.string.quick_capture_start_time), style = EbbTypography.titleM) },
                text = { TimePicker(state = pickerState) },
            )
        }

        if (showCustomDuration) {
            var customText by remember { mutableStateOf(resolved.durationMin.toString()) }
            AlertDialog(
                onDismissRequest = { showCustomDuration = false },
                confirmButton = {
                    EbbConfirmButton(
                        label = stringResource(R.string.action_save),
                        enabled = customText.toIntOrNull()?.let { it > 0 } == true,
                        onClick = {
                            customText.toIntOrNull()?.let {
                                durationOverride = it
                                durationOverridden = true
                                if (nextFreeActive) applyNextFree(it)
                            }
                            showCustomDuration = false
                        },
                    )
                },
                dismissButton = {
                    EbbCancelButton(
                        label = stringResource(R.string.action_cancel),
                        onClick = { showCustomDuration = false },
                    )
                },
                title = { Text(stringResource(R.string.quick_capture_custom_duration_title), style = EbbTypography.titleM) },
                text = {
                    OutlinedTextField(
                        value = customText,
                        onValueChange = { customText = it.filter { ch -> ch.isDigit() }.take(4) },
                        modifier = Modifier.fillMaxWidth().padding(top = EbbSpacing.s2),
                        singleLine = true,
                        label = { Text(stringResource(R.string.quick_capture_duration)) },
                    )
                },
            )
        }
    }
}
