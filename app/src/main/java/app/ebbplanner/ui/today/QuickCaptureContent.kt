@file:Suppress("LongMethod")

package app.ebbplanner.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ebbplanner.R
import app.ebbplanner.ui.theme.EbbShapes
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbSymbol
import app.ebbplanner.ui.theme.EbbTypography

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

@Composable
fun QuickCaptureSheetContent(
    state: QuickCapturePresentationState,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(bottom = EbbSpacing.s4)) {
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
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = EbbSpacing.s3),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.quick_capture_title),
                style = EbbTypography.titleL,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = {}, modifier = Modifier.size(48.dp)) {
                EbbSymbol("open_in_full", fontSize = 22.sp)
            }
        }
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
                onClick = {},
            ) {
                Text(
                    stringResource(R.string.quick_capture_paste_prompt),
                    modifier = Modifier.padding(EbbSpacing.s3),
                    style = EbbTypography.bodyM,
                )
            }
        }
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = EbbSpacing.s4, vertical = EbbSpacing.s2),
            horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
            verticalAlignment = Alignment.Top,
        ) {
            val fieldFocused = state.text.isNotEmpty() || state.mode == QuickCaptureMode.Typing
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surface, EbbShapes.sm)
                        .drawBehind {
                            val y = size.height - 2.dp.toPx()
                            drawLine(
                                color =
                                    if (fieldFocused) {
                                        androidx.compose.ui.graphics.Color(0xFF8FD1C9)
                                    } else {
                                        androidx.compose.ui.graphics.Color(0xFF3A423F)
                                    },
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = if (fieldFocused) 2.dp.toPx() else 1.dp.toPx(),
                            )
                        }
                        .padding(EbbSpacing.s3),
            ) {
                val display =
                    if (state.text.isEmpty()) {
                        stringResource(R.string.quick_capture_hint)
                    } else {
                        state.text
                    }
                if (state.highlights.isEmpty() || state.text.isEmpty()) {
                    Text(
                        display,
                        style = EbbTypography.bodyL,
                        color =
                            if (state.text.isEmpty()) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                    )
                } else {
                    Text(
                        buildAnnotatedString {
                            append(state.text)
                            state.highlights.forEach { span ->
                                addStyle(
                                    SpanStyle(color = MaterialTheme.colorScheme.primary),
                                    span.start,
                                    span.end.coerceAtMost(state.text.length),
                                )
                            }
                        },
                        style = EbbTypography.bodyL,
                    )
                }
            }
            IconButton(
                onClick = {},
                modifier = Modifier.size(48.dp),
            ) {
                Surface(shape = EbbShapes.md, color = MaterialTheme.colorScheme.surfaceVariant) {
                    EbbSymbol(
                        "mic",
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }
        }
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = EbbSpacing.s4),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            CaptureAssistChip("timer", stringResource(R.string.quick_capture_estimate))
            CaptureAssistChip("event", stringResource(R.string.quick_capture_inbox))
            CaptureAssistChip("bolt", stringResource(R.string.quick_capture_energy))
            CaptureAssistChip("palette", stringResource(R.string.quick_capture_color))
        }
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = EbbSpacing.s4, vertical = EbbSpacing.s3),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                state.counterText ?: state.footerHint,
                style = EbbTypography.labelM,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Surface(
                onClick = {},
                shape = EbbShapes.full,
                color = MaterialTheme.colorScheme.primary,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s1),
                ) {
                    EbbSymbol("arrow_upward", fontSize = 18.sp, color = MaterialTheme.colorScheme.onPrimary)
                    Text(
                        stringResource(R.string.quick_capture_add),
                        style = EbbTypography.labelL,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun CaptureAssistChip(
    icon: String,
    label: String,
) {
    Surface(
        shape = EbbShapes.full,
        color = MaterialTheme.colorScheme.surface,
        border =
            androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline,
            ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EbbSymbol(icon, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                label,
                style = EbbTypography.labelM,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        "empty" to QuickCapturePresentationState(mode = QuickCaptureMode.Empty),
        "typing" to
            QuickCapturePresentationState(
                mode = QuickCaptureMode.Typing,
                text = "Call bank about the new card",
                showKeyboardStub = true,
            ),
        "counter" to
            QuickCapturePresentationState(
                mode = QuickCaptureMode.Counter,
                text = "Buy milk",
                counterText = "12 / 120",
            ),
        "draft" to
            QuickCapturePresentationState(
                mode = QuickCaptureMode.DraftRestored,
                text = "Water plants",
                draftRestored = true,
            ),
        "paste" to
            QuickCapturePresentationState(
                mode = QuickCaptureMode.PastePrompt,
                pastePromptVisible = true,
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
