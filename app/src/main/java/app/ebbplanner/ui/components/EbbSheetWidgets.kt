@file:Suppress("NewLineAtEndOfFile", "MaxLineLength", "MatchingDeclarationName")

package app.ebbplanner.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ebbplanner.ui.theme.EbbChromeColors
import app.ebbplanner.ui.theme.EbbShapes
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbSymbol
import app.ebbplanner.ui.theme.EbbTypography

enum class EbbSegmentSelectionStyle {
    PrimaryContainer,
    SecondaryContainer,
}

@Composable
fun EbbRadioDot(
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.size(20.dp),
        shape = CircleShape,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = BorderStroke(2.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
    ) {
        if (selected) {
            Box(contentAlignment = Alignment.Center) {
                Surface(Modifier.size(8.dp), shape = CircleShape, color = MaterialTheme.colorScheme.onPrimary) {}
            }
        }
    }
}

@Composable
fun EbbJoinedSegmentedControl(
    labels: List<String>,
    icons: List<String?> = labels.map { null },
    selectedIndex: Int,
    selectionStyle: EbbSegmentSelectionStyle = EbbSegmentSelectionStyle.PrimaryContainer,
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") selectedUsesTertiary: Boolean = false,
    onSelected: (Int) -> Unit = {},
) {
    val pill = RoundedCornerShape(20.dp)
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val selectedBg =
        when (selectionStyle) {
            EbbSegmentSelectionStyle.SecondaryContainer ->
                if (dark) EbbChromeColors.segmentSelectedContainerDark else EbbChromeColors.segmentSelectedContainerLight
            EbbSegmentSelectionStyle.PrimaryContainer -> MaterialTheme.colorScheme.primaryContainer
        }
    val selectedFg =
        when (selectionStyle) {
            EbbSegmentSelectionStyle.SecondaryContainer ->
                if (dark) EbbChromeColors.onSegmentSelectedContainerDark else EbbChromeColors.onSegmentSelectedContainerLight
            EbbSegmentSelectionStyle.PrimaryContainer -> MaterialTheme.colorScheme.onPrimaryContainer
        }
    Surface(
        modifier = modifier.fillMaxWidth().height(40.dp),
        shape = pill,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            labels.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                val bg = if (selected) selectedBg else MaterialTheme.colorScheme.surface
                val fg = if (selected) selectedFg else MaterialTheme.colorScheme.onSurface
                Row(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(bg)
                            .clickable { onSelected(index) },
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    when {
                        selected -> {
                            EbbSymbol("check", fontSize = 18.sp, color = fg)
                            Text(label, style = EbbTypography.labelL, color = fg)
                        }
                        icons[index] != null -> {
                            EbbSymbol(icons[index]!!, fontSize = 18.sp, color = fg)
                            Text(label, style = EbbTypography.labelL, color = fg)
                        }
                        else -> Text(label, style = EbbTypography.labelL, color = fg)
                    }
                }
                if (index < labels.lastIndex) {
                    Box(Modifier.width(1.dp).fillMaxHeight().background(MaterialTheme.colorScheme.outline))
                }
            }
        }
    }
}

@Composable
fun EbbSegmentedChips(
    labels: List<String>,
    icons: List<String?> = labels.map { null },
    selectedIndex: Int,
    selectedUsesTertiary: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2)) {
        labels.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            val bg =
                if (selected) {
                    if (selectedUsesTertiary) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                }
            Surface(
                shape = EbbShapes.md,
                color = bg,
                border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    icons[index]?.let { EbbSymbol(it, fontSize = 16.sp) }
                    if (selected) {
                        EbbSymbol("check", fontSize = 14.sp, modifier = Modifier.padding(end = 4.dp))
                    }
                    Text(label, style = EbbTypography.labelL)
                }
            }
        }
    }
}

@Composable
fun FocusProgressRing(
    progress: Float,
    label: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    diameter: Dp = 220.dp,
    strokeWidth: Dp = 16.dp,
) {
    Box(modifier = modifier.size(diameter), contentAlignment = Alignment.Center) {
        val track = MaterialTheme.colorScheme.surfaceVariant
        val primary = MaterialTheme.colorScheme.primary
        Canvas(Modifier.matchParentSize()) {
            val stroke = strokeWidth.toPx()
            val diameter = size.minDimension - stroke
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            drawArc(
                color = track,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = stroke),
            )
            drawArc(
                color = primary,
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = EbbTypography.headlineL)
            Text(subtitle, style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun EbbSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit = {},
) {
    Row(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = EbbTypography.bodyL)
            Text(subtitle, style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors =
                SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                ),
        )
    }
}
