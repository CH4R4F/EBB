package app.ebbplanner.ui.today

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max

data class TimelineLayoutMetrics(
    val fontScale: Float = 1f,
    val titleOnlyBlocks: Boolean = false,
) {
    val largeText: Boolean get() = fontScale >= 1.75f
}

internal val timelineMinLaneWidthBase = 88.dp
internal val timelineOverflowChipWidth = 44.dp
internal val timelineFabScrollClearance = 96.dp
private val overflowChipMinHeight = 32.dp

fun timelineMinLaneWidth(metrics: TimelineLayoutMetrics): Dp {
    val scale = metrics.fontScale.coerceIn(1f, 2.5f)
    return timelineMinLaneWidthBase * scale
}

fun minBlockHeightDp(
    block: TodayScheduledBlock,
    metrics: TimelineLayoutMetrics,
): Dp {
    val large = metrics.largeText
    return when {
        block.short -> if (large) 36.dp else 24.dp
        large && !metrics.titleOnlyBlocks && block.hasMetaRow() -> 56.dp
        else -> if (block.short) 24.dp else 40.dp
    }
}


fun scheduledEndMinutes(block: TodayScheduledBlock): Int = block.startMinutes + block.durationMin

fun blocksOverlapReal(
    a: TodayScheduledBlock,
    b: TodayScheduledBlock,
): Boolean {
    val aEnd = scheduledEndMinutes(a)
    val bEnd = scheduledEndMinutes(b)
    return a.startMinutes < bEnd && b.startMinutes < aEnd
}

fun hasRealTimeOverlap(blocks: List<TodayScheduledBlock>): Boolean {
    val open = blocks.filter { !it.done }
    if (open.size < 2) return false
    for (i in open.indices) {
        val a = open[i]
        for (j in i + 1 until open.size) {
            if (blocksOverlapReal(a, open[j])) return true
        }
    }
    return false
}

class TimelineTimeMapping internal constructor(
    private val breakpointsMinutes: IntArray,
    private val yAtBreakpointsDp: FloatArray,
) {
    fun y(minutes: Int): Dp {
        if (breakpointsMinutes.isEmpty()) return timelineOriginY
        if (minutes <= breakpointsMinutes.first()) {
            return timelineOriginY
        }
        if (minutes >= breakpointsMinutes.last()) {
            return yAtBreakpointsDp.last().dp
        }
        var segment = 0
        while (segment < breakpointsMinutes.size - 1 && minutes >= breakpointsMinutes[segment + 1]) {
            segment++
        }
        val t0 = breakpointsMinutes[segment]
        val t1 = breakpointsMinutes[segment + 1]
        val y0 = yAtBreakpointsDp[segment]
        val y1 = yAtBreakpointsDp[segment + 1]
        val fraction = (minutes - t0).toFloat() / (t1 - t0).coerceAtLeast(1)
        return (y0 + (y1 - y0) * fraction).dp
    }

    fun blockHeightDp(block: TodayScheduledBlock): Dp = y(scheduledEndMinutes(block)) - y(block.startMinutes)

    fun maxContentBottomDp(): Dp = yAtBreakpointsDp.last().dp
}

fun buildTimelineOverlapPlan(
    blocks: List<TodayScheduledBlock>,
    metrics: TimelineLayoutMetrics,
    trackWidth: Dp,
    timelineStartHour: Int,
    timelineEndHour: Int = 22,
): TimelineOverlapPlan {
    val mapping = buildTimelineTimeMapping(blocks, metrics, timelineStartHour, timelineEndHour)
    return planTimelineOverlaps(blocks, metrics, trackWidth, mapping)
}

fun buildTimelineTimeMapping(
    blocks: List<TodayScheduledBlock>,
    metrics: TimelineLayoutMetrics,
    timelineStartHour: Int,
    timelineEndHour: Int,
): TimelineTimeMapping {
    val windowStart = timelineStartHour * 60
    val windowEnd = timelineEndHour * 60
    val points = sortedSetOf(windowStart, windowEnd)
    blocks.forEach { block ->
        points += block.startMinutes
        points += scheduledEndMinutes(block)
    }
    for (hour in timelineStartHour..timelineEndHour) {
        points += hour * 60
    }
    val breaks = points.toIntArray()
    val yBreaks = FloatArray(breaks.size)
    yBreaks[0] = timelineOriginY.value
    val baseRate = timelineDpPerMinute.value
    for (i in 0 until breaks.size - 1) {
        val t0 = breaks[i]
        val t1 = breaks[i + 1]
        val dt = (t1 - t0).coerceAtLeast(1)
        val mid = t0 + dt / 2
        var rate = baseRate
        blocks.forEach { block ->
            val bs = block.startMinutes
            val be = scheduledEndMinutes(block)
            if (mid in bs until be) {
                val required =
                    requiredDrawHeightDp(
                        block,
                        metrics,
                        timelineMinLaneWidth(metrics),
                    ).value
                val stretched = required / block.durationMin.toFloat()
                rate = max(rate, stretched)
            }
        }
        yBreaks[i + 1] = yBreaks[i] + rate * dt
    }
    return TimelineTimeMapping(breaks, yBreaks)
}

internal fun overlapComponents(blocks: List<TodayScheduledBlock>): List<List<TodayScheduledBlock>> {
    if (blocks.isEmpty()) return emptyList()
    val remaining = blocks.toMutableList()
    val groups = mutableListOf<List<TodayScheduledBlock>>()
    while (remaining.isNotEmpty()) {
        val seed = remaining.removeAt(0)
        val group = mutableListOf(seed)
        var i = 0
        while (i < remaining.size) {
            val candidate = remaining[i]
            val touches = group.any { blocksOverlapReal(it, candidate) }
            if (touches) {
                group += candidate
                remaining.removeAt(i)
            } else {
                i++
            }
        }
        groups += group
    }
    return groups
}

internal fun assignLanesInGroup(group: List<TodayScheduledBlock>): Map<String, Int> {
    val sorted = group.sortedBy { it.startMinutes }
    val laneEnds = mutableListOf<Int>()
    val laneById = mutableMapOf<String, Int>()
    sorted.forEach { block ->
        val end = scheduledEndMinutes(block)
        var lane = laneEnds.indexOfFirst { it <= block.startMinutes }
        if (lane < 0) {
            lane = laneEnds.size
            laneEnds.add(end)
        } else {
            laneEnds[lane] = end
        }
        laneById[block.id] = lane
    }
    return laneById
}

fun applyOverlapMetadata(blocks: List<TodayScheduledBlock>): List<TodayScheduledBlock> {
    if (blocks.isEmpty()) return blocks
    val groups = overlapComponents(blocks)
    val laneById = mutableMapOf<String, Int>()
    val countById = mutableMapOf<String, Int>()
    groups.forEach { group ->
        val lanes = assignLanesInGroup(group)
        val laneCount = (lanes.values.maxOrNull() ?: 0) + 1
        group.forEach { block ->
            laneById[block.id] = lanes[block.id] ?: 0
            countById[block.id] = if (group.size > 1) laneCount else 1
        }
    }
    return blocks.map { block ->
        block.copy(
            overlapLane = laneById[block.id] ?: 0,
            overlapLaneCount = countById[block.id] ?: 1,
        )
    }
}

data class TimelineBlockPlacement(
    val block: TodayScheduledBlock,
    val offsetXDp: Dp,
    val offsetYDp: Dp,
    val widthDp: Dp,
    val heightDp: Dp,
)

data class TimelineOverflowCluster(
    val groupKey: String,
    val allGroupBlocks: List<TodayScheduledBlock>,
    val hiddenBlocks: List<TodayScheduledBlock>,
    val offsetXDp: Dp,
    val offsetYDp: Dp,
    val widthDp: Dp,
    val heightDp: Dp,
)

data class TimelineOverlapPlan(
    val placements: List<Pair<TodayScheduledBlock, TimelineBlockPlacement>>,
    val overflow: List<TimelineOverflowCluster>,
)

fun planTimelineBottomDp(
    plan: TimelineOverlapPlan,
    mapping: TimelineTimeMapping,
): Dp {
    var bottom = mapping.maxContentBottomDp()
    plan.placements.forEach { (_, placement) ->
        val edge = placement.offsetYDp + placement.heightDp
        if (edge > bottom) bottom = edge
    }
    plan.overflow.forEach { cluster ->
        val edge = cluster.offsetYDp + cluster.heightDp
        if (edge > bottom) bottom = edge
    }
    return bottom
}

fun planTimelineOverlaps(
    blocks: List<TodayScheduledBlock>,
    metrics: TimelineLayoutMetrics,
    trackWidthDp: Dp,
    mapping: TimelineTimeMapping,
): TimelineOverlapPlan {
    val groups = overlapComponents(blocks)
    val placements = mutableListOf<Pair<TodayScheduledBlock, TimelineBlockPlacement>>()
    val overflow = mutableListOf<TimelineOverflowCluster>()
    val minLane = timelineMinLaneWidth(metrics)
    val maxVisibleLanes = (trackWidthDp / minLane).toInt().coerceAtLeast(1)

    groups.forEach { group ->
        if (group.size == 1) {
            val block = group.first()
            val top = mapping.y(block.startMinutes)
            val height = mapping.blockHeightDp(block)
            placements +=
                block to
                    TimelineBlockPlacement(
                        block = block,
                        offsetXDp = 0.dp,
                        offsetYDp = top,
                        widthDp = trackWidthDp,
                        heightDp = height,
                    )
            return@forEach
        }

        val lanes = assignLanesInGroup(group)
        val groupLaneCount = (lanes.values.maxOrNull() ?: 0) + 1
        val needsChipColumn = groupLaneCount > maxVisibleLanes
        val taskLanes =
            if (needsChipColumn) {
                (maxVisibleLanes - 1).coerceAtLeast(1)
            } else {
                kotlin.math.min(groupLaneCount, maxVisibleLanes)
            }
        val taskTrackWidth = if (needsChipColumn) trackWidthDp - timelineOverflowChipWidth else trackWidthDp
        val laneWidth = taskTrackWidth / taskLanes

        val hidden = mutableListOf<TodayScheduledBlock>()
        group.forEach { block ->
            val lane = lanes[block.id] ?: 0
            val top = mapping.y(block.startMinutes)
            val height = mapping.blockHeightDp(block)
            if (lane < taskLanes) {
                placements +=
                    block to
                        TimelineBlockPlacement(
                            block = block,
                            offsetXDp = laneWidth * lane,
                            offsetYDp = top,
                            widthDp = laneWidth - 2.dp,
                            heightDp = height,
                        )
            } else {
                hidden += block
            }
        }

        if (hidden.isNotEmpty()) {
            val chipTop = group.minOf { mapping.y(it.startMinutes) }
            val chipBottom = group.maxOf { mapping.y(scheduledEndMinutes(it)) }
            val chipHeight = (chipBottom - chipTop).coerceAtLeast(overflowChipMinHeight)
            val sortedGroup = group.sortedBy { it.startMinutes }
            overflow +=
                TimelineOverflowCluster(
                    groupKey = sortedGroup.joinToString { it.id },
                    allGroupBlocks = sortedGroup,
                    hiddenBlocks = hidden.sortedBy { it.startMinutes },
                    offsetXDp = taskTrackWidth,
                    offsetYDp = chipTop,
                    widthDp = timelineOverflowChipWidth,
                    heightDp = chipHeight,
                )
        }
    }
    return TimelineOverlapPlan(placements, overflow)
}

const val TIMELINE_OVERFLOW_CHIP_TAG_PREFIX = "timeline_overflow_"

fun timelineOverflowChipTag(groupKey: String): String = TIMELINE_OVERFLOW_CHIP_TAG_PREFIX + groupKey.hashCode().toString(16)

fun timelineOverflowSheetRowTag(blockId: String): String = "timeline_overflow_sheet_$blockId"

internal fun TodayScheduledBlock.hasMetaRow(): Boolean =
    metaTimeRange != null || metaDuration != null || minutesLeftLabel != null ||
        energyLevel != null || checklist != null || hasReminder || pinned