@file:Suppress("MagicNumber", "LongMethod", "CyclomaticComplexMethod")

package app.ebbplanner.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import app.ebbplanner.ui.theme.EbbShapes
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbSymbol
import app.ebbplanner.ui.theme.EbbTaskColors
import app.ebbplanner.ui.theme.EbbTypography

internal val timelineDpPerMinute = 72.dp / 60f
internal val timelineOriginY = 36.dp
internal val timelineHourRail = 48.dp

internal fun minutesToTimelineY(
    minutes: Int,
    startHour: Int,
): Dp {
    val origin = startHour * 60
    val offsetMin = minutes - origin
    return timelineOriginY + timelineDpPerMinute * offsetMin
}

internal fun durationToHeight(minutes: Int): Dp = timelineDpPerMinute * minutes

@Composable
fun TodayTimeline(
    state: TodayPresentationState,
    modifier: Modifier = Modifier,
) {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val hours = (state.timelineStartHour until state.timelineEndHour).toList()
    val totalMinutes = (state.timelineEndHour - state.timelineStartHour) * 60
    val baselineHeight = timelineOriginY + durationToHeight(totalMinutes)
    val contentBottom =
        state.blocks.maxOfOrNull { block ->
            minutesToTimelineY(block.startMinutes, state.timelineStartHour) +
                durationToHeight(block.durationMin)
        } ?: baselineHeight
    val trackHeight = maxOf(baselineHeight, contentBottom) + 12.dp

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(trackHeight)
                .testTag(TODAY_TIMELINE_TAG),
    ) {
        if (state.showNowLine && state.nowMinutes != null) {
            val pastHeight = minutesToTimelineY(state.nowMinutes, state.timelineStartHour)
            PastZone(
                height = pastHeight,
                modifier = Modifier.align(Alignment.TopStart),
            )
        }
        hours.forEachIndexed { index, hour ->
            val y = timelineOriginY + durationToHeight(index * 60)
            HourRule(
                label = if (state.compactHourRail) "$hour" else "%02d:00".format(hour),
                y = y,
            )
        }

        state.gaps.forEach { gap ->
            val y = minutesToTimelineY(gap.startMinutes, state.timelineStartHour)
            val h = durationToHeight(gap.durationMin).coerceAtLeast(24.dp)
            GapChip(
                label = gap.label,
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(start = timelineHourRail + 4.dp, end = 8.dp)
                        .offset(y = y)
                        .fillMaxWidth()
                        .height(h),
            )
        }

        state.events.forEach { event ->
            val y = minutesToTimelineY(event.startMinutes, state.timelineStartHour)
            val h = durationToHeight(event.durationMin).coerceAtLeast(52.dp)
            CalendarEventBlock(
                title = event.title,
                timeRange = formatRange(event.startMinutes, event.durationMin),
                durationLabel = formatDurationLabel(event.durationMin),
                calendarLabel = event.calendarLabel,
                pinned = event.pinned,
                colorKey = event.colorKey,
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(start = timelineHourRail + 4.dp, end = 8.dp)
                        .offset(y = y)
                        .fillMaxWidth()
                        .height(h),
            )
        }

        state.blocks.filter { !it.isLifted }.forEach { block ->
            val y = minutesToTimelineY(block.startMinutes, state.timelineStartHour)
            val h =
                durationToHeight(block.durationMin).coerceAtLeast(
                    if (block.short) 24.dp else 40.dp,
                )
            ScheduledTaskBlock(
                block = block,
                dark = dark,
                titleOnly = state.titleOnlyBlocks,
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(start = timelineHourRail + 4.dp, end = 8.dp)
                        .offset(y = y)
                        .fillMaxWidth()
                        .height(h)
                        .alpha(if (block.isGhost) 0.45f else 1f),
            )
        }

        state.blocks.filter { it.isLifted }.forEach { block ->
            val y = state.dragOverlay?.liftedTopDp ?: minutesToTimelineY(block.startMinutes, state.timelineStartHour)
            val h = durationToHeight(block.durationMin).coerceAtLeast(52.dp)
            ScheduledTaskBlock(
                block = block,
                dark = dark,
                titleOnly = state.titleOnlyBlocks,
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(start = timelineHourRail + 28.dp, end = 4.dp)
                        .offset(y = y)
                        .fillMaxWidth()
                        .height(h)
                        .zIndex(3f),
                elevated = true,
            )
        }

        state.dragOverlay?.let { drag ->
            val tooltipY = drag.liftedTopDp - 32.dp
            Surface(
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 120.dp)
                        .offset(y = tooltipY)
                        .zIndex(4f),
                shape = EbbShapes.sm,
                color = MaterialTheme.colorScheme.inverseSurface,
            ) {
                Text(
                    text = drag.tooltip,
                    style = EbbTypography.labelM,
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    modifier = Modifier.padding(horizontal = EbbSpacing.s2, vertical = 4.dp),
                )
            }
        }

        if (state.showNowLine && state.nowMinutes != null) {
            val y = minutesToTimelineY(state.nowMinutes, state.timelineStartHour)
            NowLine(label = formatClock(state.nowMinutes), y = y)
        }
    }
}

@Composable
private fun PastZone(
    height: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(start = timelineHourRail)
                .height(height)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
    )
}

@Composable
private fun HourRule(
    label: String,
    y: Dp,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .offset(y = y),
    ) {
        Text(
            text = label,
            style = EbbTypography.labelM,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier =
                Modifier
                    .width(timelineHourRail)
                    .padding(end = EbbSpacing.s1)
                    .align(Alignment.TopStart),
            textAlign = TextAlign.End,
        )
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = timelineHourRail, end = 8.dp)
                    .fillMaxWidth()
                    .height(1.dp)
                    .offset(y = 8.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant),
        )
    }
}

@Composable
private fun NowLine(
    label: String,
    y: Dp,
) {
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .offset(y = y)
                .zIndex(2f),
    ) {
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(primary),
        )
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = timelineHourRail - 4.dp)
                    .offset(y = (-3).dp)
                    .size(8.dp)
                    .background(primary, CircleShape),
        )
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 4.dp)
                    .offset(y = (-10).dp)
                    .height(20.dp)
                    .background(primary, EbbShapes.full)
                    .padding(horizontal = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(label, style = EbbTypography.labelM, color = onPrimary)
        }
    }
}

@Composable
private fun ScheduledTaskBlock(
    block: TodayScheduledBlock,
    dark: Boolean,
    titleOnly: Boolean,
    modifier: Modifier = Modifier,
    elevated: Boolean = false,
) {
    val accent = EbbTaskColors.strong(block.colorKey)
    val fill = EbbTaskColors.tint(block.colorKey, dark)
    val shape = if (block.short) EbbShapes.sm else EbbShapes.md
    val blockModifier =
        modifier.then(
            if (block.isCurrent) {
                Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, shape)
            } else {
                Modifier
            },
        )
    Box(modifier = blockModifier.clip(shape)) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = shape,
            color = if (block.isCurrent) fill.copy(alpha = 0.45f) else fill,
            shadowElevation = if (elevated) 8.dp else 0.dp,
        ) {
            if (block.short) {
                ShortTaskRow(block = block, accent = accent, titleOnly = titleOnly)
            } else {
                StandardTaskRow(block = block, accent = accent, titleOnly = titleOnly)
            }
        }
        if (block.isCurrent && block.minutesLeftLabel != null) {
            TimeLeftPill(
                label = block.minutesLeftLabel,
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 6.dp, bottom = 4.dp),
            )
        }
    }
}

@Composable
private fun ShortTaskRow(
    block: TodayScheduledBlock,
    accent: Color,
    titleOnly: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accent),
        )
        EbbSymbol(
            block.iconKey,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 8.dp),
        )
        Text(
            text = block.title,
            style = EbbTypography.titleM,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier =
                Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp),
        )
        if (!titleOnly) {
            val trailing = block.metaDuration ?: block.metaTimeRange
            trailing?.let {
                Text(
                    it,
                    style = EbbTypography.labelM,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    modifier = Modifier.padding(end = 4.dp),
                )
            }
        }
        if (block.selected) {
            EbbSymbol(
                "check_circle",
                fontSize = 22.sp,
                filled = true,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 8.dp),
            )
        } else {
            TaskRing(done = block.done, modifier = Modifier.padding(end = 8.dp))
        }
    }
}

@Composable
private fun StandardTaskRow(
    block: TodayScheduledBlock,
    accent: Color,
    titleOnly: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accent),
        )
        EbbSymbol(
            block.iconKey,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 8.dp),
        )
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp, vertical = 6.dp),
        ) {
            Text(
                text = block.title,
                style = EbbTypography.titleM,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val showMeta = !titleOnly && block.hasMetaRow()
            if (showMeta) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    block.metaTimeRange?.let {
                        Text(
                            it,
                            style = EbbTypography.bodyM,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                    block.metaDuration?.let {
                        Text(
                            it,
                            style = EbbTypography.bodyM,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                    block.energyLevel?.let { EnergyBars(level = it) }
                    block.checklist?.let {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            EbbSymbol("checklist", fontSize = 14.sp)
                            Text(it, style = EbbTypography.labelM, maxLines = 1)
                        }
                    }
                    if (block.hasReminder) {
                        EbbSymbol(
                            "notifications",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (block.pinned) {
                        EbbSymbol("keep", fontSize = 14.sp, filled = true)
                    }
                }
            }
        }
        if (block.selected) {
            EbbSymbol(
                "check_circle",
                fontSize = 22.sp,
                filled = true,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 8.dp),
            )
        } else {
            TaskRing(done = block.done, modifier = Modifier.padding(end = 8.dp))
        }
    }
}

@Composable
private fun TimeLeftPill(
    label: String,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    Surface(
        modifier = modifier,
        shape = EbbShapes.full,
        color = primary,
    ) {
        Text(
            label,
            style = EbbTypography.labelM,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun EnergyBars(level: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        val heights = listOf(4.dp, 6.dp, 8.dp)
        heights.forEachIndexed { index, h ->
            Box(
                modifier =
                    Modifier
                        .width(3.dp)
                        .height(h)
                        .background(
                            if (index < level) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            },
                            EbbShapes.xs,
                        ),
            )
        }
    }
}

@Composable
private fun TaskRing(
    done: Boolean,
    modifier: Modifier = Modifier,
) {
    val color =
        if (done) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline
        }
    Box(
        modifier =
            modifier
                .size(22.dp)
                .border(2.dp, color, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (done) {
            EbbSymbol("check", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun GapChip(
    label: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline,
                    EbbShapes.full,
                )
                .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = label,
            style = EbbTypography.labelM,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CalendarEventBlock(
    title: String,
    timeRange: String,
    durationLabel: String,
    calendarLabel: String,
    pinned: Boolean,
    colorKey: String,
    modifier: Modifier = Modifier,
) {
    val accent = EbbTaskColors.strong(colorKey)
    val hatch =
        Brush.linearGradient(
            colors =
                listOf(
                    Color.Transparent,
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f),
                ),
        )
    Row(
        modifier =
            modifier
                .clip(EbbShapes.md)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.5.dp, accent, EbbShapes.md)
                .drawBehind {
                    var x = 0f
                    while (x < size.width) {
                        drawLine(
                            brush = hatch,
                            start = Offset(x, 0f),
                            end = Offset(x + 8f, size.height),
                            strokeWidth = 2f,
                        )
                        x += 8f
                    }
                }
                .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
    ) {
        EbbSymbol("event", fontSize = 18.sp, color = accent)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = EbbTypography.titleM, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2)) {
                Text(timeRange, style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(durationLabel, style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (pinned) {
                    EbbSymbol("keep", fontSize = 14.sp, filled = true)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    EbbSymbol("calendar_month", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(calendarLabel, style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private fun TodayScheduledBlock.hasMetaRow(): Boolean =
    metaTimeRange != null || metaDuration != null || energyLevel != null || checklist != null ||
        hasReminder || pinned

private fun formatClock(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return "%d:%02d".format(h, m)
}

private fun formatRange(
    startMinutes: Int,
    durationMin: Int,
): String {
    val end = startMinutes + durationMin
    return "${formatClock(startMinutes)}–${formatClock(end)}"
}

private fun formatDurationLabel(durationMin: Int): String =
    when {
        durationMin >= 60 && durationMin % 60 == 0 -> "${durationMin / 60} h"
        durationMin >= 60 -> "${durationMin / 60} h ${durationMin % 60} min"
        else -> "$durationMin min"
    }
