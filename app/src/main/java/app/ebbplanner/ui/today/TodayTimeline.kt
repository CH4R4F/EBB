@file:Suppress("MagicNumber", "LongMethod", "CyclomaticComplexMethod")

package app.ebbplanner.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalConfiguration
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
import app.ebbplanner.ui.time.ClockFormat

internal val timelineDpPerMinute = 72.dp / 60f
internal val timelineOriginY = 36.dp
internal val timelineHourRail = 56.dp
internal val timelineMinLaneWidth = 96.dp

internal fun minutesToTimelineY(
    minutes: Int,
    startHour: Int,
): Dp {
    val origin = startHour * 60
    val offsetMin = minutes - origin
    return timelineOriginY + timelineDpPerMinute * offsetMin
}

internal fun durationToHeight(minutes: Int): Dp = timelineDpPerMinute * minutes

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TodayTimeline(
    state: TodayPresentationState,
    modifier: Modifier = Modifier,
    onTaskClick: (String) -> Unit = {},
    onTaskLongPress: (String) -> Unit = {},
    onMarkTaskDone: (String) -> Unit = {},
) {
    val fontScale = LocalConfiguration.current.fontScale
    val layoutMetrics =
        remember(fontScale, state.titleOnlyBlocks) {
            TimelineLayoutMetrics(fontScale = fontScale, titleOnlyBlocks = state.titleOnlyBlocks)
        }
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val largeText = layoutMetrics.largeText
    var overflowSheetBlocks by remember { mutableStateOf<List<TodayScheduledBlock>?>(null) }
    val overflowSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val hours = (state.timelineStartHour until state.timelineEndHour).toList()
    val totalMinutes = (state.timelineEndHour - state.timelineStartHour) * 60
    val visibleBlocks =
        remember(state.blocks) {
            applyOverlapMetadata(state.blocks.filter { !it.isLifted })
        }
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val trackStart = timelineHourRail + 4.dp
    val trackEnd = 8.dp
    val trackWidth = (screenWidthDp - trackStart - trackEnd).coerceAtLeast(120.dp)
    val timeMapping =
        remember(visibleBlocks, layoutMetrics, state.timelineStartHour, state.timelineEndHour) {
            buildTimelineTimeMapping(
                visibleBlocks,
                layoutMetrics,
                state.timelineStartHour,
                state.timelineEndHour,
            )
        }
    val overlapPlan =
        remember(visibleBlocks, layoutMetrics, trackWidth, timeMapping) {
            planTimelineOverlaps(visibleBlocks, layoutMetrics, trackWidth, timeMapping)
        }
    val windowEndY = timeMapping.y(state.timelineEndHour * 60)
    val planBottom = planTimelineBottomDp(overlapPlan, timeMapping) + 12.dp
    val trackHeight = maxOf(windowEndY + 12.dp, planBottom)

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(trackHeight)
                .testTag(TODAY_TIMELINE_TAG),
    ) {
        if (state.showNowLine && state.nowMinutes != null) {
            val pastHeight = timeMapping.y(state.nowMinutes)
            PastZone(
                height = pastHeight,
                modifier = Modifier.align(Alignment.TopStart),
            )
        }
        val compactRail = state.compactHourRail || largeText
        hours.forEachIndexed { index, hour ->
            val y = timeMapping.y(state.timelineStartHour * 60 + index * 60)
            val label =
                if (compactRail) {
                    hour.toString()
                } else {
                    ClockFormat.minutesToClock(hour * 60)
                }
            HourRule(
                label = label,
                y = y,
            )
        }

        state.gaps.forEach { gap ->
            val y = timeMapping.y(gap.startMinutes)
            val h =
                (timeMapping.y(gap.startMinutes + gap.durationMin) - y).coerceAtLeast(24.dp)
            GapChip(
                label = gap.label,
                largeText = largeText,
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(start = timelineHourRail + 4.dp, end = 8.dp)
                        .offset(y = y)
                        .fillMaxWidth()
                        .height(h.coerceAtLeast(if (largeText) 44.dp else 32.dp)),
            )
        }

        state.events.forEach { event ->
            val y = timeMapping.y(event.startMinutes)
            val eventHeight =
                (timeMapping.y(event.startMinutes + event.durationMin) - y).coerceAtLeast(52.dp)
            CalendarEventBlock(
                title = event.title,
                timeRange = formatRange(event.startMinutes, event.durationMin),
                durationLabel = formatDurationLabel(event.durationMin),
                calendarLabel = event.calendarLabel,
                pinned = event.pinned,
                colorKey = event.colorKey,
                largeText = largeText,
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(start = timelineHourRail + 4.dp, end = 8.dp)
                        .offset(y = y)
                        .fillMaxWidth()
                        .height(eventHeight),
            )
        }

        overlapPlan.placements.forEach { (block, placement) ->
            val compactLane = placement.widthDp < 100.dp
            val showMetaRow =
                !state.titleOnlyBlocks && !compactLane && block.hasMetaRow() &&
                    placement.heightDp >= minBlockHeightDp(block, layoutMetrics)
            ScheduledTaskBlock(
                block = block,
                dark = dark,
                titleOnly = state.titleOnlyBlocks,
                compactLane = compactLane,
                showMetaRow = showMetaRow,
                allowTwoLineTitle = compactLane && placement.heightDp >= 36.dp,
                largeText = largeText,
                onClick = { onTaskClick(block.id) },
                onLongClick = { onTaskLongPress(block.id) },
                onMarkDone = { onMarkTaskDone(block.id) },
                tallBlock = placement.heightDp >= durationToHeight(60),
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(start = trackStart, end = trackEnd)
                        .offset(x = placement.offsetXDp, y = placement.offsetYDp)
                        .width(placement.widthDp)
                        .height(placement.heightDp)
                        .zIndex(2f)
                        .alpha(
                            when {
                                block.isGhost -> 0.45f
                                block.done -> 0.55f
                                else -> 1f
                            },
                        ),
            )
        }
        overlapPlan.overflow.forEach { cluster ->
            TimelineOverflowChip(
                label = "+${cluster.hiddenBlocks.size}",
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(start = trackStart, end = trackEnd)
                        .offset(x = cluster.offsetXDp, y = cluster.offsetYDp)
                        .width(cluster.widthDp)
                        .height(cluster.heightDp)
                        .zIndex(5f)
                        .testTag(timelineOverflowChipTag(cluster.groupKey)),
                onClick = { overflowSheetBlocks = cluster.allGroupBlocks },
            )
        }

        state.blocks.filter { it.isLifted }.forEach { block ->
            val y = state.dragOverlay?.liftedTopDp ?: timeMapping.y(block.startMinutes)
            val h = timeMapping.blockHeightDp(block).coerceAtLeast(52.dp)
            ScheduledTaskBlock(
                block = block,
                dark = dark,
                titleOnly = state.titleOnlyBlocks,
                largeText = largeText,
                onClick = { onTaskClick(block.id) },
                onLongClick = { onTaskLongPress(block.id) },
                onMarkDone = { onMarkTaskDone(block.id) },
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
            val y = timeMapping.y(state.nowMinutes)
            NowLine(label = formatClock(state.nowMinutes), y = y)
        }
    }
    overflowSheetBlocks?.let { hidden ->
        ModalBottomSheet(
            onDismissRequest = { overflowSheetBlocks = null },
            sheetState = overflowSheetState,
        ) {
            Column(Modifier.padding(EbbSpacing.s4)) {
                Text("Overlapping tasks", style = EbbTypography.titleM)
                hidden.forEach { block ->
                    Surface(
                        onClick = {
                            overflowSheetBlocks = null
                            onTaskClick(block.id)
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .testTag(timelineOverflowSheetRowTag(block.id)),
                        shape = EbbShapes.md,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ) {
                        Text(
                            block.title,
                            style = EbbTypography.bodyL,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
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
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
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

@OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class,
)
@Composable
private fun TimelineOverflowChip(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = EbbShapes.sm,
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                label,
                style = EbbTypography.labelM,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
            )
        }
    }
}

@OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class,
)
@Suppress("LongParameterList")
@Composable
private fun ScheduledTaskBlock(
    block: TodayScheduledBlock,
    dark: Boolean,
    titleOnly: Boolean,
    compactLane: Boolean = false,
    showMetaRow: Boolean = false,
    allowTwoLineTitle: Boolean = false,
    largeText: Boolean = false,
    tallBlock: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onMarkDone: () -> Unit = {},
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
            modifier =
                Modifier
                    .fillMaxSize()
                    .testTag(timelineTaskTestTag(block.id))
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick,
                    ),
            shape = shape,
            color = fill,
            shadowElevation = if (elevated) 8.dp else 0.dp,
        ) {
            Box(Modifier.fillMaxSize()) {
                if (block.short) {
                    ShortTaskRow(
                        block = block,
                        accent = accent,
                        titleOnly = titleOnly,
                        compactLane = compactLane,
                        allowTwoLineTitle = allowTwoLineTitle,
                        largeText = largeText,
                        tallBlock = tallBlock,
                    )
                } else {
                    StandardTaskRow(
                        block = block,
                        accent = accent,
                        titleOnly = titleOnly,
                        compactLane = compactLane,
                        showMetaRow = showMetaRow,
                        allowTwoLineTitle = allowTwoLineTitle,
                        largeText = largeText,
                        tallBlock = tallBlock,
                    )
                }
                TaskDoneRingOverlay(block = block, onMarkDone = onMarkDone)
            }
        }
    }
}

@Composable
private fun ShortTaskRow(
    block: TodayScheduledBlock,
    accent: Color,
    titleOnly: Boolean,
    compactLane: Boolean = false,
    allowTwoLineTitle: Boolean = false,
    largeText: Boolean = false,
    tallBlock: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = if (tallBlock) Alignment.Top else Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accent),
        )
        if (!compactLane) {
            EbbSymbol(
                block.iconKey,
                fontSize = if (largeText) 16.sp else 18.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(
                        start = if (compactLane) 4.dp else 6.dp,
                        end = 22.dp,
                    ),
        ) {
            Text(
                text = block.title,
                style =
                    if (compactLane) {
                        EbbTypography.labelL
                    } else if (largeText) {
                        EbbTypography.titleM.copy(fontSize = 14.sp, lineHeight = 18.sp)
                    } else {
                        EbbTypography.titleM
                    },
                maxLines = when {
                    allowTwoLineTitle -> 2
                    largeText -> 2
                    else -> 1
                },
                overflow = if (compactLane) TextOverflow.Clip else TextOverflow.Ellipsis,
                softWrap = allowTwoLineTitle,
            )
            if (block.isCurrent && block.minutesLeftLabel != null) {
                TimeLeftPill(
                    label = block.minutesLeftLabel,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        if (!titleOnly && !compactLane) {
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
    }
}

@Composable
private fun StandardTaskRow(
    block: TodayScheduledBlock,
    accent: Color,
    titleOnly: Boolean,
    compactLane: Boolean = false,
    showMetaRow: Boolean = false,
    allowTwoLineTitle: Boolean = false,
    largeText: Boolean = false,
    tallBlock: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = if (tallBlock) Alignment.Top else Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accent),
        )
        if (!compactLane) {
            EbbSymbol(
                block.iconKey,
                fontSize = if (largeText) 16.sp else 18.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(
                        start = if (compactLane) 4.dp else 6.dp,
                        end = 22.dp,
                        top = if (largeText) 4.dp else 6.dp,
                        bottom = if (largeText) 4.dp else 6.dp,
                    ),
        ) {
            Text(
                text = block.title,
                style =
                    if (compactLane) {
                        EbbTypography.labelL
                    } else if (largeText) {
                        EbbTypography.titleM.copy(fontSize = 14.sp, lineHeight = 18.sp)
                    } else {
                        EbbTypography.titleM
                    },
                maxLines = when {
                    allowTwoLineTitle -> 2
                    largeText -> 2
                    else -> 1
                },
                overflow = if (compactLane) TextOverflow.Clip else TextOverflow.Ellipsis,
                softWrap = allowTwoLineTitle || !compactLane,
            )
            if (block.isCurrent && block.minutesLeftLabel != null) {
                TimeLeftPill(
                    label = block.minutesLeftLabel,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            if (showMetaRow) {
                if (largeText) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        block.metaTimeRange?.let {
                            Text(
                                it,
                                style = EbbTypography.bodyM.copy(fontSize = 12.sp, lineHeight = 16.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s1),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            block.metaDuration?.let {
                                Text(
                                    it,
                                    style = EbbTypography.bodyM.copy(fontSize = 12.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                )
                            }
                            block.energyLevel?.let { EnergyBars(level = it) }
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
                } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s1),
                    verticalAlignment = Alignment.Top,
                ) {
                    block.metaTimeRange?.let {
                        Text(
                            it,
                            style = EbbTypography.bodyM,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
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
        }
    }
}

@Composable
private fun TaskDoneRingOverlay(
    block: TodayScheduledBlock,
    onMarkDone: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier =
                Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 2.dp)
                    .size(48.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (block.selected) {
                EbbSymbol(
                    "check_circle",
                    fontSize = 20.sp,
                    filled = true,
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                androidx.compose.material3.IconButton(
                    onClick = onMarkDone,
                    modifier = Modifier.size(48.dp),
                ) {
                    TaskRing(done = block.done, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun TimeLeftPill(
    label: String,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    Box(
        modifier =
            modifier
                .testTag(timelineTimeLeftPillTag)
                .background(primary, EbbShapes.full)
                .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            label,
            style = EbbTypography.labelM,
            color = MaterialTheme.colorScheme.onPrimary,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
        )
    }
}

internal const val TIMELINE_TIME_LEFT_PILL_TAG = "timeline_time_left_pill"
private val timelineTimeLeftPillTag = TIMELINE_TIME_LEFT_PILL_TAG

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
            modifier.then(Modifier.size(22.dp)).border(2.dp, color, CircleShape),
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
    largeText: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val outline = MaterialTheme.colorScheme.outline
    Box(
        modifier =
            modifier.drawBehind {
                val stroke =
                    Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f),
                    )
                drawRoundRect(
                    color = outline,
                    size = size,
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                    style = stroke,
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = if (largeText) EbbTypography.labelS else EbbTypography.labelM,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = if (largeText) 2 else 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = if (largeText) 4.dp else 0.dp),
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
    largeText: Boolean = false,
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
                .padding(horizontal = 10.dp, vertical = if (largeText) 4.dp else 6.dp),
        horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
    ) {
        EbbSymbol("event", fontSize = if (largeText) 14.sp else 18.sp, color = accent)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style =
                    if (largeText) {
                        EbbTypography.titleM.copy(fontSize = 13.sp, lineHeight = 16.sp)
                    } else {
                        EbbTypography.titleM
                    },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (largeText) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s1),
                ) {
                    Text(
                        timeRange,
                        style = EbbTypography.bodyM.copy(fontSize = 11.sp, lineHeight = 14.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        durationLabel,
                        style = EbbTypography.bodyM.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                    if (pinned) {
                        EbbSymbol("keep", fontSize = 12.sp, filled = true)
                    }
                    EbbSymbol(
                        "calendar_month",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        calendarLabel,
                        style = EbbTypography.bodyM.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                }
            } else {
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
}

private fun formatClock(minutes: Int): String = ClockFormat.minutesToClock(minutes)

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
