@file:Suppress("MagicNumber", "LongMethod", "CyclomaticComplexMethod")

package app.ebbplanner.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
    val offsetMin = (minutes - origin).coerceAtLeast(0)
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
    val trackHeight = timelineOriginY + durationToHeight(totalMinutes) + 8.dp

    Box(modifier = modifier.fillMaxWidth().height(trackHeight).testTag(TODAY_TIMELINE_TAG)) {
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
            val h = durationToHeight(block.durationMin).coerceAtLeast(if (block.short) 26.dp else 44.dp)
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
                    .padding(start = timelineHourRail, end = 8.dp)
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
    val bg =
        if (block.isCurrent) {
            Brush.verticalGradient(
                0f to MaterialTheme.colorScheme.surface,
                0.56f to MaterialTheme.colorScheme.surface,
                0.56f to fill,
                1f to fill,
            )
        } else {
            null
        }
    val container = if (block.isCurrent) Color.Transparent else fill
    val shape = if (block.short) EbbShapes.sm else EbbShapes.md
    Surface(
        modifier = modifier,
        shape = shape,
        color = container,
        shadowElevation = if (elevated) 8.dp else 0.dp,
    ) {
        Row(
            modifier =
                Modifier
                    .then(
                        if (bg != null) {
                            Modifier.drawBehind {
                                drawRect(brush = bg)
                            }
                        } else {
                            Modifier
                        },
                    )
                    .height(IntrinsicSize.Min),
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
                        .padding(horizontal = 6.dp, vertical = if (block.short) 2.dp else 6.dp),
            ) {
                Text(
                    text = block.title,
                    style = EbbTypography.titleM,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!titleOnly && (block.metaTimeRange != null || block.metaDuration != null)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2)) {
                        block.metaTimeRange?.let {
                            Text(it, style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        block.metaDuration?.let {
                            Text(it, style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        block.energyLevel?.let { EnergyDots(level = it) }
                        block.checklist?.let {
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                EbbSymbol("checklist", fontSize = 14.sp)
                                Text(it, style = EbbTypography.labelM)
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
            block.minutesLeftLabel?.let {
                Text(
                    it,
                    style = EbbTypography.labelM,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 4.dp),
                )
            }
            if (block.selected) {
                EbbSymbol("check_circle", fontSize = 22.sp, filled = true, color = MaterialTheme.colorScheme.primary)
            } else {
                TaskRing(done = block.done, modifier = Modifier.padding(end = 8.dp))
            }
        }
    }
}

@Composable
private fun EnergyDots(level: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(3) { index ->
            Box(
                modifier =
                    Modifier
                        .size(width = 4.dp, height = 8.dp)
                        .background(
                            if (index < level) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
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
        modifier = modifier,
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
