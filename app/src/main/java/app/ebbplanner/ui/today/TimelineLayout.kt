package app.ebbplanner.ui.today

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
import kotlin.math.min

data class TimelineLayoutMetrics(
    val fontScale: Float = 1f,
    val titleOnlyBlocks: Boolean = false,
) {
    val largeText: Boolean get() = fontScale >= 1.75f
}

internal val timelineMinLaneWidthBase = 88.dp
internal val timelineOverflowChipWidth = 44.dp
private val overflowChipHeight = 36.dp

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
    if (blocks.size < 2) return false
    for (i in blocks.indices) {
        val a = blocks[i]
        for (j in i + 1 until blocks.size) {
            if (blocksOverlapReal(a, blocks[j])) return true
        }
    }
    return false
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

fun blockDrawHeightDp(
    block: TodayScheduledBlock,
    allBlocks: List<TodayScheduledBlock>,
    metrics: TimelineLayoutMetrics,
    timelineStartHour: Int,
): Dp {
    val natural =
        durationToHeight(block.durationMin).coerceAtLeast(minBlockHeightDp(block, metrics))
    val blockTop = minutesToTimelineY(block.startMinutes, timelineStartHour)
    val next =
        allBlocks
            .asSequence()
            .filter { it.id != block.id && it.startMinutes >= block.startMinutes }
            .filter { !blocksOverlapReal(block, it) }
            .minByOrNull { it.startMinutes }
    if (next != null) {
        val nextTop = minutesToTimelineY(next.startMinutes, timelineStartHour)
        val gap = nextTop - blockTop
        if (gap > 0.dp) {
            return min(natural.value, gap.value).dp
        }
    }
    return natural
}

data class TimelineBlockPlacement(
    val block: TodayScheduledBlock,
    val offsetXDp: Dp,
    val widthDp: Dp,
    val heightDp: Dp,
)

data class TimelineOverflowCluster(
    val groupKey: String,
    val startMinutes: Int,
    val allGroupBlocks: List<TodayScheduledBlock>,
    val hiddenBlocks: List<TodayScheduledBlock>,
    val offsetXDp: Dp,
    val widthDp: Dp,
    val heightDp: Dp,
)

data class TimelineOverlapPlan(
    val placements: List<Pair<TodayScheduledBlock, TimelineBlockPlacement>>,
    val overflow: List<TimelineOverflowCluster>,
)

fun planTimelineBottomDp(
    plan: TimelineOverlapPlan,
    timelineStartHour: Int,
    metrics: TimelineLayoutMetrics,
): Dp {
    var bottom = 0.dp
    plan.placements.forEach { (block, placement) ->
        val y = minutesToTimelineY(block.startMinutes, timelineStartHour)
        val edge = y + placement.heightDp
        if (edge > bottom) bottom = edge
    }
    plan.overflow.forEach { cluster ->
        val y = minutesToTimelineY(cluster.startMinutes, timelineStartHour)
        val edge = y + cluster.heightDp
        if (edge > bottom) bottom = edge
    }
    return bottom
}

fun planTimelineOverlaps(
    blocks: List<TodayScheduledBlock>,
    metrics: TimelineLayoutMetrics,
    trackWidthDp: Dp,
    timelineStartHour: Int,
): TimelineOverlapPlan {
    val groups = overlapComponents(blocks)
    val placements = mutableListOf<Pair<TodayScheduledBlock, TimelineBlockPlacement>>()
    val overflow = mutableListOf<TimelineOverflowCluster>()
    val minLane = timelineMinLaneWidth(metrics)
    val maxVisibleLanes = (trackWidthDp / minLane).toInt().coerceAtLeast(1)

    groups.forEach { group ->
        if (group.size == 1) {
            val block = group.first()
            val height = blockDrawHeightDp(block, blocks, metrics, timelineStartHour)
            placements +=
                block to
                    TimelineBlockPlacement(
                        block = block,
                        offsetXDp = 0.dp,
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
                min(groupLaneCount, maxVisibleLanes)
            }
        val taskTrackWidth = if (needsChipColumn) trackWidthDp - timelineOverflowChipWidth else trackWidthDp
        val laneWidth = taskTrackWidth / taskLanes

        val hidden = mutableListOf<TodayScheduledBlock>()
        group.forEach { block ->
            val lane = lanes[block.id] ?: 0
            val height = blockDrawHeightDp(block, blocks, metrics, timelineStartHour)
            if (lane < taskLanes) {
                placements +=
                    block to
                        TimelineBlockPlacement(
                            block = block,
                            offsetXDp = laneWidth * lane,
                            widthDp = laneWidth - 2.dp,
                            heightDp = height,
                        )
            } else {
                hidden += block
            }
        }

        if (hidden.isNotEmpty()) {
            val groupEndMin = group.maxOf { scheduledEndMinutes(it) }
            val sortedGroup = group.sortedBy { it.startMinutes }
            overflow +=
                TimelineOverflowCluster(
                    groupKey = sortedGroup.joinToString { it.id },
                    startMinutes = groupEndMin,
                    allGroupBlocks = sortedGroup,
                    hiddenBlocks = hidden.sortedBy { it.startMinutes },
                    offsetXDp = taskTrackWidth,
                    widthDp = timelineOverflowChipWidth,
                    heightDp = overflowChipHeight.coerceAtLeast(minBlockHeightDp(hidden.first(), metrics)),
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