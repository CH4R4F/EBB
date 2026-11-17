@file:Suppress("LongMethod", "NewLineAtEndOfFile", "MaxLineLength", "UnusedPrivateProperty", "MagicNumber")

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
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
import app.ebbplanner.ui.sheets.SheetActions
import app.ebbplanner.ui.theme.EbbShapes
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbSymbol
import app.ebbplanner.ui.theme.EbbTaskColors
import app.ebbplanner.ui.theme.EbbTypography
import app.ebbplanner.ui.today.TodayScreenDesign
import app.ebbplanner.ui.today.todayPopulatedDark

private val colorLabels =
    mapOf(
        "amber" to "Amber",
        "sky" to "Sky",
        "green" to "Green",
        "lemon" to "Lemon",
        "blue" to "Blue",
        "vermillion" to "Vermillion",
        "rose" to "Rose",
        "slate" to "Slate",
    )

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
        modifier = modifier.fillMaxWidth().height(720.dp),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 6.dp,
    ) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.size(width = 32.dp, height = 4.dp).background(MaterialTheme.colorScheme.outline, EbbShapes.full))
            }
            Text("Color & icon", style = EbbTypography.titleL, modifier = Modifier.padding(horizontal = EbbSpacing.s4))
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(horizontal = EbbSpacing.s4, vertical = EbbSpacing.s2),
            ) {
                TaskPreviewBlock()
                Text(
                    "Color",
                    style = EbbTypography.labelL,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = EbbSpacing.s3, bottom = 8.dp),
                )
                ColorSwatches(state)
                Text(
                    "Icon",
                    style = EbbTypography.labelL,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = EbbSpacing.s3, bottom = 8.dp),
                )
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = EbbShapes.full,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = EbbSpacing.s3, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        EbbSymbol("search", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            state.searchQuery.ifEmpty { "Search icons" },
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
            SheetActions()
        }
    }
}

@Composable
private fun TaskPreviewBlock() {
    Surface(
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = EbbShapes.md,
        color = EbbTaskColors.tint("amber", dark = true),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(width = 4.dp, height = 56.dp).background(EbbTaskColors.strong("amber")))
            EbbSymbol("edit_note", fontSize = 20.sp, modifier = Modifier.padding(horizontal = 8.dp))
            Column {
                Text("Write project brief", style = EbbTypography.labelL)
                Text("10:00–11:15 · 1 h 15 min", style = EbbTypography.labelM, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ColorSwatches(state: ColorIconPresentationState) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.height(160.dp),
    ) {
        items(state.colorOptions) { key ->
            val sel = key == state.selectedColor
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = EbbTaskColors.strong(key),
                    border = if (sel) BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface) else null,
                ) {
                    if (sel) {
                        Box(contentAlignment = Alignment.Center) {
                            Surface(Modifier.size(26.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surface) {
                                Box(contentAlignment = Alignment.Center) {
                                    EbbSymbol("check", fontSize = 18.sp)
                                }
                            }
                        }
                    }
                }
                Text(
                    colorLabels[key] ?: key,
                    style = EbbTypography.labelM,
                    color = if (sel) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IconGrid(state: ColorIconPresentationState) {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(top = 8.dp),
    ) {
        state.iconOptions.take(28).forEach { icon ->
            val sel = icon == state.selectedIcon
            Surface(
                modifier = Modifier.size(48.dp),
                shape = EbbShapes.md,
                color = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    EbbSymbol(
                        icon,
                        fontSize = 22.sp,
                        color = if (sel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    )
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
