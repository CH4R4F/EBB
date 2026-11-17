@file:Suppress("LongMethod", "NewLineAtEndOfFile", "MaxLineLength", "UnusedPrivateProperty")

package app.ebbplanner.ui.coloricon

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ebbplanner.ui.theme.EbbShapes
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbSymbol
import app.ebbplanner.ui.theme.EbbTaskColors
import app.ebbplanner.ui.theme.EbbTypography
import app.ebbplanner.ui.today.TodayScreenDesign
import app.ebbplanner.ui.today.todayPopulatedDark

@Composable
fun ColorIconPickerScaffold(
    state: ColorIconPresentationState,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        TodayScreenDesign(state = todayPopulatedDark(), showBottomNav = true, modifier = Modifier.fillMaxSize())
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f)),
        )
        ColorIconSheet(state, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun ColorIconSheet(
    state: ColorIconPresentationState,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 6.dp,
    ) {
        Column(modifier = Modifier.padding(EbbSpacing.s4)) {
            Text("Color & icon", style = EbbTypography.titleL)
            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = EbbSpacing.s2),
                shape = EbbShapes.full,
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = EbbSpacing.s3, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    EbbSymbol("search", fontSize = 20.sp)
                    Text(
                        state.searchQuery.ifEmpty { "Search icons…" },
                        modifier = Modifier.padding(start = EbbSpacing.s2),
                        style = EbbTypography.bodyL,
                        color =
                            if (state.searchQuery.isEmpty()) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                    )
                }
            }
            ColorSwatches(state)
            if (state.mode == ColorIconMode.SearchEmpty) {
                Text(
                    "No icons match “${state.searchQuery}”",
                    style = EbbTypography.bodyM,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = EbbSpacing.s4),
                )
            } else {
                IconGrid(state)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorSwatches(state: ColorIconPresentationState) {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    FlowRow(horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2)) {
        state.colorOptions.forEach { key ->
            val sel = key == state.selectedColor
            Surface(
                modifier = Modifier.size(40.dp),
                shape = EbbShapes.md,
                color = EbbTaskColors.strong(key),
                border = if (sel) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
            ) {}
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IconGrid(state: ColorIconPresentationState) {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    FlowRow(horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2), verticalArrangement = Arrangement.spacedBy(EbbSpacing.s2)) {
        state.iconOptions.forEach { icon ->
            val sel = icon == state.selectedIcon
            Surface(
                modifier = Modifier.size(48.dp),
                shape = EbbShapes.md,
                color = if (sel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    EbbSymbol(icon, fontSize = 22.sp)
                }
            }
        }
    }
}

@Composable
fun ColorIconAppPreview(
    darkTheme: Boolean,
    state: ColorIconPresentationState,
) {
    app.ebbplanner.ui.onboarding.OnboardingAppPreview(darkTheme = darkTheme) {
        ColorIconPickerScaffold(state = state)
    }
}
