@file:Suppress("LongMethod", "LongParameterList", "CyclomaticComplexMethod")

package app.ebbplanner.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.ebbplanner.R
import app.ebbplanner.domain.capture.QuickCaptureParser
import app.ebbplanner.ui.components.EbbSheetActions
import app.ebbplanner.ui.text.LocalTextOverflowReports
import app.ebbplanner.ui.theme.EbbShapes
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbTypography

const val QUICK_CAPTURE_PREVIEW_TAG = "quick_capture_preview"
const val QUICK_CAPTURE_DESTINATION_TAG = "quick_capture_destination"
const val QUICK_CAPTURE_START_CHIP_TAG = "quick_capture_start_chip"
const val QUICK_CAPTURE_NOW_TAG = "quick_capture_now"
const val QUICK_CAPTURE_NEXT_FREE_TAG = "quick_capture_next_free"
const val QUICK_CAPTURE_CLEAR_TIME_TAG = "quick_capture_clear_time"
const val QUICK_CAPTURE_DURATION_PREFIX = "quick_capture_duration_"
const val QUICK_CAPTURE_CUSTOM_DURATION_TAG = "quick_capture_duration_custom"

fun quickCaptureDurationTag(min: Int): String = QUICK_CAPTURE_DURATION_PREFIX + min

internal val quickCaptureDurationPresets = listOf(15, 30, 45, 60, 90)

@Composable
fun QuickCaptureOverlay(
    capture: QuickCapturePresentationState,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = EbbShapes.lg,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shadowElevation = 8.dp,
        ) {
            QuickCaptureSheetContent(capture)
        }
        if (capture.showKeyboardStub) {
            QuickCaptureKeyboardStub()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickCaptureSheetContent(
    state: QuickCapturePresentationState,
    modifier: Modifier = Modifier,
    showDragHandle: Boolean = true,
    interactive: Boolean = false,
    text: String = state.text,
    onTextChange: (String) -> Unit = {},
    onAdd: () -> Unit = {},
    onImeDone: () -> Unit = onAdd,
    onCancel: () -> Unit = {},
    onAppendEstimate: (String) -> Unit = {},
    onPickStart: () -> Unit = {},
    onClearStart: () -> Unit = {},
    onNow: () -> Unit = {},
    onNextFree: () -> Unit = {},
    onSelectDuration: (Int) -> Unit = {},
    onCustomDuration: () -> Unit = {},
) {
    val overflow = LocalTextOverflowReports.current
    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .padding(bottom = EbbSpacing.s4),
    ) {
        if (showDragHandle) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = EbbSpacing.s3, vertical = EbbSpacing.s2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                        Modifier
                            .padding(top = 6.dp)
                            .size(width = 40.dp, height = 4.dp)
                            .background(MaterialTheme.colorScheme.outline, EbbShapes.full),
                )
            }
        }
        Text(
            stringResource(R.string.quick_capture_title),
            style = EbbTypography.titleL,
            modifier = Modifier.padding(horizontal = EbbSpacing.s4, vertical = EbbSpacing.s1),
        )
        if (state.draftRestored) {
            Text(
                stringResource(R.string.quick_capture_draft_restored),
                style = EbbTypography.labelM,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = EbbSpacing.s4),
            )
        }
        if (state.pastePromptVisible) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(EbbSpacing.s3),
                shape = EbbShapes.md,
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    stringResource(R.string.quick_capture_paste_prompt),
                    modifier = Modifier.padding(EbbSpacing.s3),
                    style = EbbTypography.bodyM,
                )
            }
        }
        val highlights = if (interactive) QuickCaptureParser.parse(text).highlights else state.highlights
        QuickCaptureField(
            text = if (interactive) text else state.text,
            highlights = highlights,
            interactive = interactive,
            focused = text.isNotEmpty() || state.mode == QuickCaptureMode.Typing,
            onTextChange = onTextChange,
            onImeDone = onImeDone,
        )
        val previewCd = stringResource(R.string.quick_capture_preview_cd)
        val destCdPrefix = stringResource(R.string.quick_capture_dest_cd)
        val startTimeLabelText = stringResource(R.string.quick_capture_start_time)
        val nowLabel = stringResource(R.string.quick_capture_now)
        val nextFreeLabel = stringResource(R.string.quick_capture_next_free)
        val customDurationLabel = stringResource(R.string.quick_capture_custom_duration)
        state.previewLabel?.let { preview ->
            SuggestionChip(
                onClick = onPickStart,
                label = {
                    Text(
                        preview,
                        style = EbbTypography.labelM,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                modifier =
                    Modifier
                        .padding(horizontal = EbbSpacing.s4, vertical = EbbSpacing.s1)
                        .testTag(QUICK_CAPTURE_PREVIEW_TAG)
                        .semantics { contentDescription = previewCd },
            )
        }
        val destination = state.destinationLabel.ifBlank { state.footerHint }
        Text(
            destination,
            style = EbbTypography.bodyM,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier =
                Modifier
                    .padding(horizontal = EbbSpacing.s4, vertical = EbbSpacing.s1)
                    .testTag(QUICK_CAPTURE_DESTINATION_TAG)
                    .semantics { contentDescription = destCdPrefix + ": " + destination },
            maxLines = 2,
            onTextLayout = { overflow?.put(QUICK_CAPTURE_DESTINATION_TAG, it.hasVisualOverflow) },
        )
        Text(
            stringResource(R.string.quick_capture_start_time),
            style = EbbTypography.labelL,
            modifier = Modifier.padding(start = EbbSpacing.s4, top = EbbSpacing.s2, end = EbbSpacing.s4),
        )
        FlowRow(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = EbbSpacing.s4),
            horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
            verticalArrangement = Arrangement.spacedBy(EbbSpacing.s1),
        ) {
            val startLabel = state.startTimeLabel ?: stringResource(R.string.quick_capture_add_time)
            FilterChip(
                selected = state.startTimeLabel != null,
                onClick = onPickStart,
                label = { Text(startLabel, maxLines = 1, style = EbbTypography.labelM) },
                modifier =
                    Modifier
                        .testTag(QUICK_CAPTURE_START_CHIP_TAG)
                        .semantics { contentDescription = startTimeLabelText + ": " + startLabel },
            )
            if (state.showTodayShortcuts && state.startTimeLabel == null) {
                FilterChip(
                    selected = false,
                    onClick = onNow,
                    label = { Text(nowLabel, maxLines = 1, style = EbbTypography.labelM) },
                    modifier =
                        Modifier
                            .testTag(QUICK_CAPTURE_NOW_TAG)
                            .semantics { contentDescription = nowLabel },
                )
                FilterChip(
                    selected = false,
                    onClick = onNextFree,
                    label = { Text(nextFreeLabel, maxLines = 1, style = EbbTypography.labelM) },
                    modifier =
                        Modifier
                            .testTag(QUICK_CAPTURE_NEXT_FREE_TAG)
                            .semantics { contentDescription = nextFreeLabel },
                )
            }
            if (state.startTimeLabel != null) {
                FilterChip(
                    selected = false,
                    onClick = onClearStart,
                    label = { Text(stringResource(R.string.quick_capture_clear_time), maxLines = 1, style = EbbTypography.labelM) },
                    modifier = Modifier.testTag(QUICK_CAPTURE_CLEAR_TIME_TAG),
                )
            }
        }
        Text(
            stringResource(R.string.quick_capture_duration),
            style = EbbTypography.labelL,
            modifier = Modifier.padding(start = EbbSpacing.s4, top = EbbSpacing.s3, end = EbbSpacing.s4),
        )
        FlowRow(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = EbbSpacing.s4),
            horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
            verticalArrangement = Arrangement.spacedBy(EbbSpacing.s1),
        ) {
            quickCaptureDurationPresets.forEach { minutes ->
                val selected = state.selectedDurationMin == minutes
                val durationLabel = stringResource(R.string.quick_capture_duration_min, minutes)
                FilterChip(
                    selected = selected,
                    onClick = { if (interactive) onSelectDuration(minutes) else onAppendEstimate("${minutes}min") },
                    label = {
                        Text(
                            durationLabel,
                            maxLines = 1,
                            style = EbbTypography.labelM,
                        )
                    },
                    modifier =
                        Modifier
                            .testTag(quickCaptureDurationTag(minutes))
                            .semantics { contentDescription = durationLabel },
                )
            }
            val customSelected =
                state.selectedDurationMin != null && state.selectedDurationMin !in quickCaptureDurationPresets
            FilterChip(
                selected = customSelected,
                onClick = onCustomDuration,
                label = {
                    Text(
                        if (customSelected) {
                            stringResource(R.string.quick_capture_duration_min, state.selectedDurationMin ?: 0)
                        } else {
                            stringResource(R.string.quick_capture_custom_duration)
                        },
                        maxLines = 1,
                        style = EbbTypography.labelM,
                    )
                },
                modifier =
                    Modifier
                        .testTag(QUICK_CAPTURE_CUSTOM_DURATION_TAG)
                        .semantics { contentDescription = customDurationLabel },
            )
        }
        EbbSheetActions(
            confirmLabel = stringResource(R.string.action_save),
            onConfirm = onAdd,
            onCancel = onCancel,
            confirmEnabled = if (interactive) text.isNotBlank() else state.saveEnabled,
            confirmTestTag = QUICK_CAPTURE_ADD_BUTTON_TAG,
            modifier = Modifier.padding(horizontal = EbbSpacing.s4, vertical = EbbSpacing.s3),
        )
    }
}

@Composable
private fun QuickCaptureField(
    text: String,
    highlights: List<QuickCaptureHighlight>,
    interactive: Boolean,
    focused: Boolean,
    onTextChange: (String) -> Unit,
    onImeDone: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = EbbSpacing.s4, vertical = EbbSpacing.s2)
                .background(MaterialTheme.colorScheme.surface, EbbShapes.sm)
                .drawBehind {
                    val y = size.height - 2.dp.toPx()
                    drawLine(
                        color =
                            if (focused) {
                                androidx.compose.ui.graphics.Color(0xFF8FD1C9)
                            } else {
                                androidx.compose.ui.graphics.Color(0xFF3A423F)
                            },
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = if (focused) 2.dp.toPx() else 1.dp.toPx(),
                    )
                }
                .padding(EbbSpacing.s3),
    ) {
        if (interactive) {
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                textStyle = EbbTypography.bodyL.copy(color = MaterialTheme.colorScheme.onSurface),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onImeDone() }),
                modifier = Modifier.fillMaxWidth().testTag(QUICK_CAPTURE_TEXT_FIELD_TAG),
                decorationBox = { inner ->
                    if (text.isEmpty()) {
                        Text(
                            stringResource(R.string.quick_capture_hint),
                            style = EbbTypography.bodyL,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    inner()
                },
            )
        } else if (highlights.isEmpty() || text.isEmpty()) {
            Text(
                if (text.isEmpty()) stringResource(R.string.quick_capture_hint) else text,
                style = EbbTypography.bodyL,
                color =
                    if (text.isEmpty()) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
            )
        } else {
            Text(
                buildAnnotatedString {
                    append(text)
                    highlights.forEach { span ->
                        addStyle(
                            SpanStyle(color = MaterialTheme.colorScheme.primary),
                            span.start,
                            span.end.coerceAtMost(text.length),
                        )
                    }
                },
                style = EbbTypography.bodyL,
            )
        }
    }
}

@Composable
private fun QuickCaptureKeyboardStub() {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(horizontal = EbbSpacing.s2, vertical = EbbSpacing.s2),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, EbbShapes.sm),
        )
        repeat(4) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surfaceVariant, EbbShapes.sm),
            )
        }
    }
}

fun quickCaptureFixtures(): Map<String, QuickCapturePresentationState> =
    mapOf(
        "empty" to
            QuickCapturePresentationState(
                mode = QuickCaptureMode.Empty,
                destinationLabel = "Anytime (no time set)",
                showTodayShortcuts = true,
            ),
        "typing" to
            QuickCapturePresentationState(
                mode = QuickCaptureMode.Typing,
                text = "Call bank about the new card",
                showKeyboardStub = true,
                destinationLabel = "Anytime (no time set)",
                showTodayShortcuts = true,
                saveEnabled = true,
            ),
        "counter" to
            QuickCapturePresentationState(
                mode = QuickCaptureMode.Counter,
                text = "Buy milk",
                counterText = "12 / 120",
                destinationLabel = "Anytime (no time set)",
                saveEnabled = true,
            ),
        "draft" to
            QuickCapturePresentationState(
                mode = QuickCaptureMode.DraftRestored,
                text = "Water plants",
                draftRestored = true,
                destinationLabel = "Anytime (no time set)",
                saveEnabled = true,
            ),
        "paste" to
            QuickCapturePresentationState(
                mode = QuickCaptureMode.PastePrompt,
                pastePromptVisible = true,
                destinationLabel = "Anytime (no time set)",
            ),
        "nl" to
            QuickCapturePresentationState(
                mode = QuickCaptureMode.NaturalLanguage,
                text = "Dentist tomorrow 3pm 45min",
                highlights =
                    listOf(
                        QuickCaptureHighlight(8, 16, QuickCaptureHighlightKind.Tomorrow),
                        QuickCaptureHighlight(17, 21, QuickCaptureHighlightKind.Time),
                        QuickCaptureHighlight(22, 27, QuickCaptureHighlightKind.Duration),
                    ),
                previewLabel = "Tomorrow 15:00 · 45 min",
                destinationLabel = "Timeline, tomorrow 15:00",
                startTimeLabel = "15:00",
                selectedDurationMin = 45,
                saveEnabled = true,
            ),
        "parsed" to
            QuickCapturePresentationState(
                mode = QuickCaptureMode.NaturalLanguage,
                text = "gym 18:00 45m",
                previewLabel = "Today 18:00 · 45 min",
                destinationLabel = "Timeline, today 18:00",
                startTimeLabel = "18:00",
                selectedDurationMin = 45,
                saveEnabled = true,
            ),
        "timeSet" to
            QuickCapturePresentationState(
                mode = QuickCaptureMode.Typing,
                text = "gym",
                previewLabel = "Today 18:00 · 30 min",
                destinationLabel = "Timeline, today 18:00",
                startTimeLabel = "18:00",
                selectedDurationMin = 30,
                saveEnabled = true,
            ),
    )

@Composable
fun QuickCaptureAppPreview(
    darkTheme: Boolean,
    capture: QuickCapturePresentationState,
) {
    val state = todayWithQuickCapture(capture)
    TodayAppPreview(darkTheme = darkTheme, state = state)
}

@Composable
fun QuickCaptureStandalonePreview(
    darkTheme: Boolean,
    capture: QuickCapturePresentationState,
) {
    app.ebbplanner.ui.onboarding.OnboardingAppPreview(darkTheme = darkTheme) {
        QuickCaptureSheetContent(capture, modifier = Modifier.fillMaxWidth())
    }
}
