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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import app.ebbplanner.ui.text.LocalTextOverflowReports
import app.ebbplanner.ui.theme.EbbShapes
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbSymbol
import app.ebbplanner.ui.theme.EbbTaskColors
import app.ebbplanner.ui.theme.EbbTypography
import app.ebbplanner.ui.time.ClockFormat

internal val timelineDpPerMinute = 72.dp / 60f
internal val timelineOriginY = 36.dp
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

internal const val TIMELINE_NOW_BADGE_TAG = "timeline_now_badge"
internal const val TIMELINE_HOUR_LABEL_TAG = "timeline_hour_label"

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
    val is24Hour = ClockFormat.isSystem24Hour(LocalContext.current)
    val compactRail = state.compactHourRail || largeText
    val hourRail = rememberTimelineHourRailWidth(compactRail, fontScale, is24Hour)
    val hourLabelMeasurer = androidx.compose.ui.text.rememberTextMeasurer()
    val hourLabelDensity = androidx.compose.ui.platform.LocalDensity.current
    val hourLabelMaxWidth = hourRail - EbbSpacing.s1
    val badgeHeight = nowBadgeHeightDp(layoutMetrics)
    val trackStart = hourRail + 4.dp
    val trackEnd = 8.dp
    val trackWidth = timelineTrackWidthDp(screenWidthDp, hourRail, trackEnd)
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
                hourRail = hourRail,
                modifier = Modifier.align(Alignment.TopStart),
            )
        }
        val nowY =
            if (state.showNowLine && state.nowMinutes != null) {
                timeMapping.y(state.nowMinutes)
            } else {
                null
            }
        hours.forEachIndexed { index, hour ->
            val hourMin = state.timelineStartHour * 60 + index * 60
            val y = timeMapping.y(hourMin)
            val hideHourLabel = nowY != null && nowBadgeOverlapsHourLabel(nowY, y, badgeHeight)
            val label =
                if (hideHourLabel) {
                    null
                } else {
                    pickHourGutterLabel(
                        hourLabelMeasurer,
                        hourLabelDensity,
                        hour,
                        is24Hour,
                        hourLabelMaxWidth,
                    )
                }
            HourRule(
                label = label,
                y = y,
                hourRail = hourRail,
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
                        .padding(start = trackStart, end = trackEnd)
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
                timeRange =
                    formatRange(
                        event.startMinutes,
                        event.durationMin,
                        ClockFormat.isSystem24Hour(LocalContext.current),
                    ),
                durationLabel = formatDurationLabel(event.durationMin),
                calendarLabel = event.calendarLabel,
                pinned = event.pinned,
                colorKey = event.colorKey,
                largeText = largeText,
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(start = trackStart, end = trackEnd)
                        .offset(y = y)
                        .fillMaxWidth()
                        .height(eventHeight),
            )
        }

        if (state.showNowLine && state.nowMinutes != null) {
            val y = timeMapping.y(state.nowMinutes)
            NowLineStrike(y = y, hourRail = hourRail)
        }

        overlapPlan.placements.forEach { (block, placement) ->
            val compactLane = placement.widthDp < 100.dp
            val contentPlan =
                planBlockContent(
                    block = block,
                    metrics = layoutMetrics,
                    blockHeightDp = placement.heightDp,
                    laneWidthDp = placement.widthDp,
                    compactLane = compactLane,
                    titleOnly = state.titleOnlyBlocks,
                    ringSlotWidth = timelineRingSlotWidth(layoutMetrics),
                )
            ScheduledTaskBlock(
                block = block,
                dark = dark,
                titleOnly = state.titleOnlyBlocks,
                compactLane = compactLane,
                contentPlan = contentPlan,
                layoutMetrics = layoutMetrics,
                ringSlotWidth = timelineRingSlotWidth(layoutMetrics),
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
                        .then(if (block.isGhost) Modifier.alpha(0.45f) else Modifier),
                contentDimmed = block.done,
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
            val liftedPlan =
                planBlockContent(
                    block = block,
                    metrics = layoutMetrics,
                    blockHeightDp = h,
                    laneWidthDp = trackWidth,
                    compactLane = false,
                    titleOnly = state.titleOnlyBlocks,
                    ringSlotWidth = timelineRingSlotWidth(layoutMetrics),
                )
            ScheduledTaskBlock(
                block = block,
                dark = dark,
                titleOnly = state.titleOnlyBlocks,
                compactLane = false,
                contentPlan = liftedPlan,
                layoutMetrics = layoutMetrics,
                ringSlotWidth = timelineRingSlotWidth(layoutMetrics),
                largeText = largeText,
                onClick = { onTaskClick(block.id) },
                onLongClick = { onTaskLongPress(block.id) },
                onMarkDone = { onMarkTaskDone(block.id) },
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(start = hourRail + 24.dp, end = 4.dp)
                        .offset(y = y)
                        .fillMaxWidth()
                        .height(h)
                        .zIndex(3f),
                elevated = true,
            )
        }

        if (state.showNowLine && state.nowMinutes != null) {
            NowLineTimeBadge(
                label = ClockFormat.minutesToClockCompact(state.nowMinutes, is24Hour),
                y = timeMapping.y(state.nowMinutes),
                hourRail = hourRail,
                badgeHeight = badgeHeight,
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
    hourRail: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(start = hourRail)
                .height(height)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
    )
}

@Composable
private fun HourRule(
    label: String?,
    y: Dp,
    hourRail: Dp,
) {
    val overflowReports = LocalTextOverflowReports.current
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .offset(y = y),
    ) {
        if (label != null) {
            Text(
                text = label,
                style = timelineHourLabelTextStyle(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier =
                    Modifier
                        .width(hourRail)
                        .padding(end = EbbSpacing.s1)
                        .align(Alignment.TopStart)
                        .testTag(TIMELINE_HOUR_LABEL_TAG),
                textAlign = TextAlign.End,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
                onTextLayout = { layout ->
                    overflowReports?.let { reports ->
                        reports[TIMELINE_HOUR_LABEL_TAG] =
                            reports[TIMELINE_HOUR_LABEL_TAG] == true ||
                            layout.didOverflowWidth ||
                            layout.isLineEllipsized(0)
                    }
                },
            )
        }
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = hourRail, end = 8.dp)
                    .fillMaxWidth()
                    .height(1.dp)
                    .offset(y = 8.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant),
        )
    }
}

@Composable
private fun NowLineStrike(
    y: Dp,
    hourRail: Dp,
) {
    val primary = MaterialTheme.colorScheme.primary
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .offset(y = y)
                .zIndex(1f),
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
                    .padding(start = hourRail - 4.dp)
                    .offset(y = (-3).dp)
                    .size(8.dp)
                    .background(primary, CircleShape),
        )
    }
}

@Composable
private fun NowLineTimeBadge(
    label: String,
    y: Dp,
    hourRail: Dp,
    badgeHeight: Dp,
) {
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val overflowReports = LocalTextOverflowReports.current
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .offset(y = y - badgeHeight / 2)
                .zIndex(6f),
    ) {
        Surface(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 2.dp)
                    .widthIn(max = hourRail - 4.dp)
                    .wrapContentWidth()
                    .heightIn(min = badgeHeight),
            shape = EbbShapes.full,
            color = primary,
            shadowElevation = 1.dp,
        ) {
            Text(
                label,
                style = timelineNowBadgeTextStyle(),
                color = onPrimary,
                maxLines = 1,
                softWrap = false,
                onTextLayout = { overflowReports?.put(TIMELINE_NOW_BADGE_TAG, it.hasVisualOverflow) },
                modifier =
                    Modifier
                        .testTag(TIMELINE_NOW_BADGE_TAG)
                        .padding(horizontal = 6.dp, vertical = 3.dp),
            )
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

@Suppress("LongParameterList")
internal fun timelineTaskTitleTag(blockId: String): String = "timeline_task_title_$blockId"

internal fun timelineRingSlotTag(blockId: String): String = "timeline_ring_slot_$blockId"

internal fun timelineTaskDurationTag(blockId: String): String = "timeline_task_duration_$blockId"

internal fun timelineTaskRangeTag(blockId: String): String = "timeline_task_range_$blockId"

@Composable
private fun TimelineRangeText(
    block: TodayScheduledBlock,
    style: androidx.compose.ui.text.TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val range = block.metaTimeRange ?: return
    val is24Hour = ClockFormat.isSystem24Hour(LocalContext.current)
    val overflowReports = LocalTextOverflowReports.current
    val tag = timelineTaskRangeTag(block.id)
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = modifier) {
        val textMeasurer = androidx.compose.ui.text.rememberTextMeasurer()
        val density = androidx.compose.ui.platform.LocalDensity.current
        val display =
            androidx.compose.runtime.remember(
                range,
                block.startMinutes,
                is24Hour,
                maxWidth,
                density,
                textMeasurer,
                style,
            ) {
                pickTimeRangeLabel(
                    textMeasurer,
                    density,
                    range,
                    block.startMinutes,
                    is24Hour,
                    maxWidth,
                    style,
                )
            }
        Text(
            display,
            style = style,
            color = color,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .testTag(tag),
            onTextLayout = { layout ->
                overflowReports?.put(
                    tag,
                    layout.didOverflowWidth || layout.isLineEllipsized(0),
                )
            },
        )
    }
}

@OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class,
)
@Composable
private fun ScheduledTaskBlock(
    block: TodayScheduledBlock,
    dark: Boolean,
    titleOnly: Boolean,
    compactLane: Boolean = false,
    contentPlan: BlockContentPlan,
    layoutMetrics: TimelineLayoutMetrics,
    ringSlotWidth: Dp,
    largeText: Boolean = false,
    tallBlock: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onMarkDone: () -> Unit = {},
    modifier: Modifier = Modifier,
    elevated: Boolean = false,
    contentDimmed: Boolean = false,
) {
    val accent = EbbTaskColors.strong(block.colorKey).let { if (contentDimmed) it.copy(alpha = 0.55f) else it }
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
            modifier = Modifier.fillMaxSize().testTag(timelineTaskTestTag(block.id)),
            shape = shape,
            color = fill,
            shadowElevation = if (elevated) 8.dp else 0.dp,
        ) {
            if (block.short) {
                ShortTaskRow(
                    block = block,
                    accent = accent,
                    compactLane = compactLane,
                    contentPlan = contentPlan,
                    layoutMetrics = layoutMetrics,
                    ringSlotWidth = ringSlotWidth,
                    largeText = largeText,
                    tallBlock = tallBlock,
                    onClick = onClick,
                    onLongClick = onLongClick,
                    onMarkDone = onMarkDone,
                    contentDimmed = contentDimmed,
                )
            } else {
                StandardTaskRow(
                    block = block,
                    accent = accent,
                    compactLane = compactLane,
                    contentPlan = contentPlan,
                    layoutMetrics = layoutMetrics,
                    ringSlotWidth = ringSlotWidth,
                    largeText = largeText,
                    tallBlock = tallBlock,
                    onClick = onClick,
                    onLongClick = onLongClick,
                    onMarkDone = onMarkDone,
                    contentDimmed = contentDimmed,
                )
            }
        }
    }
}

@Composable
private fun ShortTaskRow(
    block: TodayScheduledBlock,
    accent: Color,
    compactLane: Boolean = false,
    contentPlan: BlockContentPlan,
    layoutMetrics: TimelineLayoutMetrics,
    ringSlotWidth: Dp,
    largeText: Boolean = false,
    tallBlock: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onMarkDone: () -> Unit = {},
    contentDimmed: Boolean = false,
) {
    val textAlpha = if (contentDimmed) 0.55f else 1f
    TaskBlockRowShell(
        block = block,
        accent = accent,
        compactLane = compactLane,
        contentPlan = contentPlan,
        ringSlotWidth = ringSlotWidth,
        largeText = largeText,
        tallBlock = tallBlock,
        onClick = onClick,
        onLongClick = onLongClick,
        onMarkDone = onMarkDone,
        contentTextAlpha = textAlpha,
    ) {
        contentPlan.pillLabel?.let {
            TimeLeftPillInColumn(
                blockId = block.id,
                label = it,
                lineHeightDp = pillHeightDp(layoutMetrics),
                modifier = Modifier.padding(top = pillGapDp()),
            )
        }
        if (contentPlan.showDurationLine) {
            if (block.metaDuration != null) {
                Text(
                    block.metaDuration,
                    style = EbbTypography.labelM,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = textAlpha),
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.testTag(timelineTaskDurationTag(block.id)),
                )
            } else {
                block.metaTimeRange?.let {
                    TimelineRangeText(
                        block = block,
                        style = EbbTypography.labelM,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = textAlpha),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun StandardTaskRow(
    block: TodayScheduledBlock,
    accent: Color,
    compactLane: Boolean = false,
    contentPlan: BlockContentPlan,
    layoutMetrics: TimelineLayoutMetrics,
    ringSlotWidth: Dp,
    largeText: Boolean = false,
    tallBlock: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onMarkDone: () -> Unit = {},
    contentDimmed: Boolean = false,
) {
    val textAlpha = if (contentDimmed) 0.55f else 1f
    TaskBlockRowShell(
        block = block,
        accent = accent,
        compactLane = compactLane,
        contentPlan = contentPlan,
        ringSlotWidth = ringSlotWidth,
        largeText = largeText,
        tallBlock = tallBlock,
        onClick = onClick,
        onLongClick = onLongClick,
        onMarkDone = onMarkDone,
        standardVerticalPadding = true,
        contentTextAlpha = textAlpha,
    ) {
        contentPlan.pillLabel?.let {
            TimeLeftPillInColumn(
                blockId = block.id,
                label = it,
                lineHeightDp = pillHeightDp(layoutMetrics),
                modifier = Modifier.padding(top = pillGapDp()),
            )
        }
        if (contentPlan.showMetaRow) {
                if (largeText) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        block.metaTimeRange?.let {
                            TimelineRangeText(
                                block = block,
                                style = EbbTypography.bodyM.copy(fontSize = 12.sp, lineHeight = 16.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = textAlpha),
                                modifier = Modifier.fillMaxWidth(),
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
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = textAlpha),
                                    maxLines = 1,
                                    modifier = Modifier.testTag(timelineTaskDurationTag(block.id)),
                                )
                            }
                            block.energyLevel?.let { EnergyBars(level = it) }
                            if (block.hasReminder) {
                                EbbSymbol(
                                    "notifications",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = textAlpha),
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
                        TimelineRangeText(
                            block = block,
                            style = EbbTypography.bodyM,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = textAlpha),
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (block.overlapLaneCount <= 1 || block.metaTimeRange == null) {
                        block.metaDuration?.let {
                            Text(
                                it,
                                style = EbbTypography.bodyM,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = textAlpha),
                                maxLines = 1,
                                modifier = Modifier.testTag(timelineTaskDurationTag(block.id)),
                            )
                        }
                    }
                    block.energyLevel?.let { EnergyBars(level = it) }
                    block.checklist?.let {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            EbbSymbol("checklist", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = textAlpha))
                            Text(it, style = EbbTypography.labelM, maxLines = 1, color = MaterialTheme.colorScheme.onSurface.copy(alpha = textAlpha))
                        }
                    }
                    if (block.hasReminder) {
                        EbbSymbol(
                            "notifications",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = textAlpha),
                        )
                    }
                    if (block.pinned) {
                        EbbSymbol("keep", fontSize = 14.sp, filled = true, color = MaterialTheme.colorScheme.onSurface.copy(alpha = textAlpha))
                    }
                }
                }
            }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun TaskBlockRowShell(
    block: TodayScheduledBlock,
    accent: Color,
    compactLane: Boolean,
    contentPlan: BlockContentPlan,
    ringSlotWidth: Dp,
    largeText: Boolean,
    tallBlock: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMarkDone: () -> Unit,
    standardVerticalPadding: Boolean = false,
    contentTextAlpha: Float = 1f,
    belowTitle: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    val onSurface = MaterialTheme.colorScheme.onSurface.copy(alpha = contentTextAlpha)
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentTextAlpha)
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = if (tallBlock) Alignment.Top else Alignment.CenterVertically,
    ) {
        val contentClick =
            Modifier
                .weight(1f)
                .fillMaxHeight()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                )
        Row(
            modifier = contentClick,
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
                color = onSurface,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(
                        start = if (compactLane) 4.dp else 6.dp,
                        end = 4.dp,
                        top = if (standardVerticalPadding && largeText) 4.dp else if (standardVerticalPadding) 6.dp else 0.dp,
                        bottom =
                            when {
                                contentPlan.pillLabel != null -> pillBottomInsetDp()
                                standardVerticalPadding && largeText -> 4.dp
                                standardVerticalPadding -> 6.dp
                                else -> 0.dp
                            },
                    ),
        ) {
            Text(
                text = block.title,
                modifier = Modifier.testTag(timelineTaskTitleTag(block.id)),
                style =
                    if (compactLane) {
                        EbbTypography.labelL
                    } else if (largeText) {
                        EbbTypography.titleM.copy(fontSize = 14.sp, lineHeight = 18.sp)
                    } else {
                        EbbTypography.titleM
                    },
                color = onSurface,
                maxLines = contentPlan.titleMaxLines,
                overflow = TextOverflow.Ellipsis,
                softWrap = contentPlan.titleMaxLines > 1,
            )
            belowTitle()
        }
        }
        TaskDoneRingSlot(
            block = block,
            slotWidth = ringSlotWidth,
            onMarkDone = onMarkDone,
        )
    }
}

@Composable
private fun TaskDoneRingSlot(
    block: TodayScheduledBlock,
    slotWidth: Dp,
    onMarkDone: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .width(slotWidth)
                .fillMaxHeight()
                .heightIn(min = slotWidth)
                .testTag(timelineRingSlotTag(block.id)),
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
                modifier = Modifier.fillMaxSize(),
            ) {
                TaskRing(done = block.done, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun TimeLeftPillInColumn(
    blockId: String,
    label: String,
    lineHeightDp: Dp,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
    ) {
        val maxTextWidth = (maxWidth - timeLeftPillHorizontalPadding).coerceAtLeast(0.dp)
        TimeLeftPill(
            blockId = blockId,
            label = label,
            maxTextWidth = maxTextWidth,
            lineHeightDp = lineHeightDp,
        )
    }
}

@Composable
private fun TimeLeftPill(
    blockId: String,
    label: String,
    maxTextWidth: Dp,
    lineHeightDp: Dp,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    val textMeasurer = androidx.compose.ui.text.rememberTextMeasurer()
    val density = androidx.compose.ui.platform.LocalDensity.current
    val display =
        androidx.compose.runtime.remember(label, maxTextWidth, density, textMeasurer) {
            pickTimeLeftPillLabel(textMeasurer, density, label, maxTextWidth)
        }
    val chosenLabels = LocalTimelinePillChosenLabels.current
    val lineEllipsized = LocalTimelinePillLineEllipsized.current
    androidx.compose.runtime.SideEffect {
        chosenLabels?.put(blockId, display)
    }
    Box(
        modifier =
            modifier
                .heightIn(min = lineHeightDp)
                .testTag(timelineTimeLeftPillTag(blockId))
                .background(primary, EbbShapes.full)
                .padding(horizontal = 8.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            display,
            style = EbbTypography.labelM,
            color = MaterialTheme.colorScheme.onPrimary,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { layout ->
                lineEllipsized?.put(blockId, layout.isLineEllipsized(0))
            },
        )
    }
}

internal const val TIMELINE_TIME_LEFT_PILL_TAG_PREFIX = "timeline_time_left_pill_"

internal fun timelineTimeLeftPillTag(blockId: String): String = TIMELINE_TIME_LEFT_PILL_TAG_PREFIX + blockId

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

private fun formatRange(
    startMinutes: Int,
    durationMin: Int,
    is24Hour: Boolean,
): String {
    val end = startMinutes + durationMin
    return ClockFormat.rangeDisplay(startMinutes, end, is24Hour)
}

private fun formatDurationLabel(durationMin: Int): String =
    when {
        durationMin >= 60 && durationMin % 60 == 0 -> "${durationMin / 60} h"
        durationMin >= 60 -> "${durationMin / 60} h ${durationMin % 60} min"
        else -> "$durationMin min"
    }
