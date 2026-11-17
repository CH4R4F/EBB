@file:Suppress("LongMethod", "MagicNumber", "MaxLineLength", "NewLineAtEndOfFile")

package app.ebbplanner.ui.taskdetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ebbplanner.ui.theme.EbbShapes
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbSymbol
import app.ebbplanner.ui.theme.EbbTypography
import app.ebbplanner.ui.today.TodayScreenDesign
import app.ebbplanner.ui.today.todayPopulatedDark

@Composable
fun EstimatePickerScaffold(
    state: EstimatePickerState,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        TodayScreenDesign(
            state = todayPopulatedDark(),
            showBottomNav = true,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f)),
        )
        EstimatePickerSheet(
            state = state,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun EstimatePickerSheet(
    state: EstimatePickerState,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 6.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SheetGrabber()
            Text(
                text = state.sheetTitle,
                style = EbbTypography.titleL,
                modifier = Modifier.padding(horizontal = EbbSpacing.s4),
            )
            if (state.sheetSubtitle != null) {
                Text(
                    text = state.sheetSubtitle,
                    style = EbbTypography.bodyM,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier =
                        Modifier.padding(
                            horizontal = EbbSpacing.s4,
                            vertical = EbbSpacing.s1,
                        ),
                )
            } else {
                Box(modifier = Modifier.height(EbbSpacing.s2))
            }
            when (state.mode) {
                EstimatePickerMode.Presets -> PresetsBody(state)
                EstimatePickerMode.Custom -> CustomWheelBody(state, showBreakdown = false)
                EstimatePickerMode.BigPrompt -> CustomWheelBody(state, showBreakdown = true)
            }
            SheetActions(doneLabel = state.doneLabel)
        }
    }
}

@Composable
private fun SheetGrabber() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .width(32.dp)
                    .height(4.dp)
                    .clip(EbbShapes.full)
                    .background(MaterialTheme.colorScheme.outline),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PresetsBody(state: EstimatePickerState) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = EbbSpacing.s4),
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
            verticalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
        ) {
            state.presetOptionsMinutes.forEach { min ->
                PresetChip(
                    label = formatPreset(min),
                    selected = min == state.selectedPresetMinutes,
                )
            }
        }
        Row(
            modifier = Modifier.padding(top = EbbSpacing.s3, bottom = EbbSpacing.s2),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s1),
        ) {
            EbbSymbol("tune", fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
            Text(
                "Custom…",
                style = EbbTypography.labelL,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CustomWheelBody(
    state: EstimatePickerState,
    showBreakdown: Boolean,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = EbbSpacing.s4),
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
            verticalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
        ) {
            state.presetOptionsMinutes.forEach { min ->
                PresetChip(label = formatPreset(min), selected = false)
            }
        }
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = EbbSpacing.s3),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DurationWheelColumn(
                above = state.hourAbove,
                value = state.customHours.toString(),
                unit = "h",
                below = state.hourBelow,
            )
            DurationWheelColumn(
                above = state.minuteAbove,
                value = state.customMinutes.toString(),
                unit = "min",
                below = state.minuteBelow,
                modifier = Modifier.padding(start = EbbSpacing.s4),
            )
        }
        if (showBreakdown) {
            BreakdownPromptCard()
        }
    }
}

@Composable
private fun BreakdownPromptCard() {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = EbbSpacing.s2),
        shape = EbbShapes.md,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(modifier = Modifier.padding(EbbSpacing.s3)) {
            Row(verticalAlignment = Alignment.Top) {
                EbbSymbol("call_split", fontSize = 22.sp)
                Column(modifier = Modifier.padding(start = EbbSpacing.s3)) {
                    Text("Big one! Break it into steps?", style = EbbTypography.titleM)
                    Text(
                        "Small steps are easier to start.",
                        style = EbbTypography.bodyM,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = EbbSpacing.s2),
                horizontalArrangement = Arrangement.End,
            ) {
                Surface(shape = EbbShapes.full, color = MaterialTheme.colorScheme.primaryContainer) {
                    Text(
                        "Break it down",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        style = EbbTypography.labelL,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}

@Composable
private fun DurationWheelColumn(
    above: String,
    value: String,
    unit: String,
    below: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.width(96.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            above,
            style = EbbTypography.titleM,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
        )
        Surface(
            modifier = Modifier.padding(vertical = EbbSpacing.s1),
            shape = EbbShapes.md,
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Row(
                modifier =
                    Modifier
                        .width(96.dp)
                        .height(56.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    value,
                    style = EbbTypography.headlineL,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    unit,
                    style = EbbTypography.bodyL,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        }
        Text(
            below,
            style = EbbTypography.titleM,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PresetChip(
    label: String,
    selected: Boolean,
) {
    val chipShape = RoundedCornerShape(12.dp)
    Surface(
        shape = chipShape,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (selected) {
                EbbSymbol("check", fontSize = 16.sp)
            }
            Text(label, style = EbbTypography.labelL)
        }
    }
}

@Composable
private fun SheetActions(doneLabel: String) {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = EbbSpacing.s2, vertical = EbbSpacing.s2),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "Cancel",
            style = EbbTypography.labelL,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = EbbSpacing.s3, vertical = EbbSpacing.s2),
        )
        Surface(
            shape = EbbShapes.full,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = EbbSpacing.s2),
        ) {
            Text(
                doneLabel,
                style = EbbTypography.labelL,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            )
        }
    }
}

private fun formatPreset(min: Int): String =
    when (min) {
        60 -> "1 h"
        90 -> "1.5 h"
        120 -> "2 h"
        else -> "$min min"
    }

@Deprecated("Use EstimatePickerScaffold for sheet layout", ReplaceWith("EstimatePickerScaffold(state, modifier)"))
@Composable
fun EstimatePickerDesign(
    state: EstimatePickerState,
    modifier: Modifier = Modifier,
) {
    EstimatePickerScaffold(state, modifier)
}

@Composable
fun EstimatePickerAppPreview(
    darkTheme: Boolean,
    state: EstimatePickerState,
) {
    app.ebbplanner.ui.onboarding.OnboardingAppPreview(darkTheme = darkTheme) {
        EstimatePickerScaffold(state = state)
    }
}
