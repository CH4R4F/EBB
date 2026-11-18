package app.ebbplanner.ui.today

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.ceil

data class TimelineLayoutMetrics(
    val fontScale: Float = 1f,
    val titleOnlyBlocks: Boolean = false,
) {
    val largeText: Boolean get() = fontScale >= 1.75f
}

internal val timelineMinLaneWidthBase = 88.dp

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

fun minDrawMinutes(
    block: TodayScheduledBlock,
    metrics: TimelineLayoutMetrics,
): Int {
    val minDp = minBlockHeightDp(block, metrics)
    return ceil(minDp.value / timelineDpPerMinute.value).toInt()
}

fun effectiveTimelineEndMinutes(
    block: TodayScheduledBlock,
    metrics: TimelineLayoutMetrics = TimelineLayoutMetrics(),
): Int {
    val floor = minDrawMinutes(block, metrics)
    return block.startMinutes + maxOf(block.durationMin, floor)
}

internal fun blocksOverlapDrawn(
    a: TodayScheduledBlock,
    b: TodayScheduledBlock,
    metrics: TimelineLayoutMetrics,
): Boolean {
    val aEnd = effectiveTimelineEndMinutes(a, metrics)
    val bEnd = effectiveTimelineEndMinutes(b, metrics)
    return a.startMinutes < bEnd && b.startMinutes < aEnd
}

internal fun overlapComponents(
    blocks: List<TodayScheduledBlock>,
    metrics: TimelineLayoutMetrics,
): List<List<TodayScheduledBlock>> {
    if (blocks.isEmpty()) return emptyList()
    val remaining = blocks.toMutableList()
    val groups = mutableListOf<List<TodayScheduledBlock>>()
    while (remaining.isNotEmpty()) {
        val seed = remaining.removeAt(0)
        val group = mutableListOf(seed)
        var i = 0
        while (i < remaining.size) {
            val candidate = remaining[i]
            val touches = group.any { blocksOverlapDrawn(it, candidate, metrics) }
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

internal fun assignLanesInGroup(
    group: List<TodayScheduledBlock>,
    metrics: TimelineLayoutMetrics,
): Map<String, Int> {
    val sorted = group.sortedBy { it.startMinutes }
    val laneEnds = mutableListOf<Int>()
    val laneById = mutableMapOf<String, Int>()
    sorted.forEach { block ->
        val end = effectiveTimelineEndMinutes(block, metrics)
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

fun applyOverlapMetadata(
    blocks: List<TodayScheduledBlock>,
    metrics: TimelineLayoutMetrics = TimelineLayoutMetrics(),
): List<TodayScheduledBlock> {
    if (blocks.isEmpty()) return blocks
    val groups = overlapComponents(blocks, metrics)
    val laneById = mutableMapOf<String, Int>()
    val countById = mutableMapOf<String, Int>()
    groups.forEach { group ->
        val lanes = assignLanesInGroup(group, metrics)
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
    val widthDp: Dp,
    val heightDp: Dp,
)

data class TimelineOverflowCluster(
    val groupKey: String,
    val startMinutes: Int,
    val hiddenBlocks: List<TodayScheduledBlock>,
    val offsetXDp: Dp,
    val widthDp: Dp,
    val heightDp: Dp,
)

data class TimelineOverlapPlan(
    val placements: List<Pair<TodayScheduledBlock, TimelineBlockPlacement>>,
    val overflow: List<TimelineOverflowCluster>,
)

fun planTimelineOverlaps(
    blocks: List<TodayScheduledBlock>,
    metrics: TimelineLayoutMetrics,
    trackWidthDp: Dp,
    timelineStartHour: Int,
): TimelineOverlapPlan {
    val groups = overlapComponents(blocks, metrics)
    val placements = mutableListOf<Pair<TodayScheduledBlock, TimelineBlockPlacement>>()
    val overflow = mutableListOf<TimelineOverflowCluster>()
    val minLane = timelineMinLaneWidth(metrics)
    val maxVisibleLanes = (trackWidthDp / minLane).toInt().coerceAtLeast(1)

    groups.forEach { group ->
        val lanes = assignLanesInGroup(group, metrics)
        val groupLaneCount = (lanes.values.maxOrNull() ?: 0) + 1
        val visibleLanes = minOf(groupLaneCount, maxVisibleLanes)
        val laneWidth = trackWidthDp / visibleLanes

        val hidden = mutableListOf<TodayScheduledBlock>()
        group.forEach { block ->
            val lane = lanes[block.id] ?: 0
            val height = durationToHeight(block.durationMin).coerceAtLeast(minBlockHeightDp(block, metrics))
            if (group.size == 1) {
                placements +=
                    block to
                        TimelineBlockPlacement(
                            block = block,
                            offsetXDp = 0.dp,
                            widthDp = trackWidthDp,
                            heightDp = height,
                        )
            } else if (lane < visibleLanes) {
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
            val startMin = hidden.minOf { it.startMinutes }
            val chipLane = (visibleLanes - 1).coerceAtLeast(0)
            overflow +=
                TimelineOverflowCluster(
                    groupKey = group.sortedBy { it.startMinutes }.joinToString { it.id },
                    startMinutes = startMin,
                    hiddenBlocks = hidden.sortedBy { it.startMinutes },
                    offsetXDp = laneWidth * chipLane,
                    widthDp = laneWidth - 2.dp,
                    heightDp = 40.dp.coerceAtLeast(minBlockHeightDp(hidden.first(), metrics)),
                )
        }
    }
    return TimelineOverlapPlan(placements, overflow)
}

const val TIMELINE_OVERFLOW_CHIP_TAG_PREFIX = "timeline_overflow_"

fun timelineOverflowChipTag(groupKey: String): String = TIMELINE_OVERFLOW_CHIP_TAG_PREFIX + groupKey.hashCode().toString(16)

internal fun TodayScheduledBlock.hasMetaRow(): Boolean =
    metaTimeRange != null || metaDuration != null || minutesLeftLabel != null ||
        energyLevel != null || checklist != null || hasReminder || pinned