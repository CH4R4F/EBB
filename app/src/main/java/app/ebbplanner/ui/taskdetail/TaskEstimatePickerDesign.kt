@file:Suppress("LongMethod", "NewLineAtEndOfFile")

package app.ebbplanner.ui.taskdetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.ebbplanner.ui.theme.EbbShapes
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbTypography

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EstimatePickerDesign(
    state: EstimatePickerState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(EbbSpacing.s4),
    ) {
        val title =
            when (state.mode) {
                EstimatePickerMode.BigPrompt -> state.promptTitle
                else -> "Estimate"
            }
        Text(title, style = EbbTypography.titleL)
        when (state.mode) {
            EstimatePickerMode.Presets ->
                FlowRow(
                    modifier = Modifier.padding(top = EbbSpacing.s3),
                    horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
                ) {
                    state.options.forEach { min ->
                        val sel = min == state.selectedMinutes
                        Surface(
                            shape = EbbShapes.full,
                            color =
                                if (sel) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                },
                            border =
                                if (sel) {
                                    null
                                } else {
                                    BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                },
                        ) {
                            Text(
                                formatMin(min),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                style = EbbTypography.labelL,
                            )
                        }
                    }
                }
            EstimatePickerMode.Custom ->
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(top = EbbSpacing.s3),
                    shape = EbbShapes.md,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        state.customText,
                        modifier = Modifier.padding(EbbSpacing.s4),
                        style = EbbTypography.headlineM,
                    )
                }
            EstimatePickerMode.BigPrompt ->
                Text(
                    "Pick a duration or type your own",
                    modifier = Modifier.padding(top = EbbSpacing.s3),
                    style = EbbTypography.bodyL,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
        }
    }
}

private fun formatMin(min: Int): String =
    when {
        min >= 60 && min % 60 == 0 -> "${min / 60} h"
        min >= 60 -> "${min / 60} h ${min % 60} min"
        else -> "$min min"
    }

@Composable
fun EstimatePickerAppPreview(
    darkTheme: Boolean,
    state: EstimatePickerState,
) {
    app.ebbplanner.ui.onboarding.OnboardingAppPreview(darkTheme = darkTheme) {
        EstimatePickerDesign(state)
    }
}
