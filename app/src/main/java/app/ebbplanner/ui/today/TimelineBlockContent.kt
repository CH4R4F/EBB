package app.ebbplanner.ui.today

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun timelineRingSlotWidth(metrics: TimelineLayoutMetrics): Dp =
    if (metrics.largeText) {
        48.dp
    } else {
        40.dp
    }

private fun scaledLineDp(
    lineSp: Float,
    fontScale: Float,
): Dp = (lineSp * fontScale).dp

fun titleLineHeightDp(
    metrics: TimelineLayoutMetrics,
    compactLane: Boolean,
): Dp {
    val lineSp =
        when {
            compactLane && metrics.largeText -> 18f
            compactLane -> 20f
            metrics.largeText -> 18f
            else -> 24f
        }
    return scaledLineDp(lineSp, metrics.fontScale)
}

fun metaRowHeightDp(metrics: TimelineLayoutMetrics): Dp =
    if (metrics.largeText) {
        scaledLineDp(16f + 12f + 2f, metrics.fontScale)
    } else {
        scaledLineDp(20f, metrics.fontScale)
    }

fun pillHeightDp(metrics: TimelineLayoutMetrics): Dp =
    scaledLineDp(16f, metrics.fontScale) + 4.dp

fun pillGapDp(): Dp = 2.dp

fun blockVerticalPaddingDp(
    metrics: TimelineLayoutMetrics,
    short: Boolean,
): Dp =
    when {
        short -> 0.dp
        metrics.largeText -> 8.dp
        else -> 12.dp
    }

fun formatTimeLeftPillLabel(
    label: String,
    narrow: Boolean,
): String {
    if (!narrow) return label
    val match = Regex("(\\d+)\\s*min").find(label) ?: return label
    return "${match.groupValues[1]}m"
}

fun useNarrowTimeLeftPill(contentWidthDp: Dp): Boolean = contentWidthDp < 88.dp

data class BlockContentPlan(
    val titleMaxLines: Int,
    val showMetaRow: Boolean,
    val pillLabel: String?,
    val showDurationLine: Boolean,
    val contentWidthDp: Dp,
)

fun timelineBlockContentWidth(
    laneWidthDp: Dp,
    metrics: TimelineLayoutMetrics,
    compactLane: Boolean,
): Dp {
    val ring = timelineRingSlotWidth(metrics)
    val icon = if (compactLane) 0.dp else 26.dp
    val horizontal = if (compactLane) 8.dp else 12.dp
    return (laneWidthDp - 4.dp - ring - icon - horizontal).coerceAtLeast(0.dp)
}

fun planBlockContent(
    block: TodayScheduledBlock,
    metrics: TimelineLayoutMetrics,
    blockHeightDp: Dp,
    laneWidthDp: Dp,
    compactLane: Boolean,
    titleOnly: Boolean,
): BlockContentPlan {
    val contentWidth = timelineBlockContentWidth(laneWidthDp, metrics, compactLane)
    val titleLine = titleLineHeightDp(metrics, compactLane)
    val vPad = blockVerticalPaddingDp(metrics, block.short)
    val pillH =
        if (block.isCurrent && block.minutesLeftLabel != null) {
            pillHeightDp(metrics) + pillGapDp()
        } else {
            0.dp
        }
    val narrowPill = useNarrowTimeLeftPill(contentWidth)
    val pillLabel =
        block.minutesLeftLabel?.let {
            formatTimeLeftPillLabel(it, narrowPill)
        }

    val metaH = metaRowHeightDp(metrics)
  val oneLineContent = vPad + titleLine + pillH
    val twoLineContent = vPad + titleLine * 2 + pillH
    val metaContent = vPad + titleLine + metaH + pillH

    val showMeta =
        !titleOnly &&
            !compactLane &&
            block.hasMetaRow() &&
            blockHeightDp >= metaContent

    val titleMaxLines =
        if (!showMeta && blockHeightDp >= twoLineContent && (compactLane || metrics.largeText)) {
            2
        } else {
            1
        }

    val showDuration =
        !titleOnly &&
            !compactLane &&
            block.short &&
            (block.metaDuration != null || block.metaTimeRange != null) &&
            !showMeta &&
            blockHeightDp >= oneLineContent + scaledLineDp(16f, metrics.fontScale)

    return BlockContentPlan(
        titleMaxLines = titleMaxLines,
        showMetaRow = showMeta,
        pillLabel = if (block.isCurrent) pillLabel else null,
        showDurationLine = showDuration,
        contentWidthDp = contentWidth,
    )
}

fun requiredDrawHeightDp(
    block: TodayScheduledBlock,
    metrics: TimelineLayoutMetrics,
    laneWidthDp: Dp = 360.dp,
): Dp {
    val compact = laneWidthDp < 100.dp
    val plan =
        planBlockContent(
            block = block,
            metrics = metrics,
            blockHeightDp = 10_000.dp,
            laneWidthDp = laneWidthDp,
            compactLane = compact,
            titleOnly = metrics.titleOnlyBlocks,
        )
    var floor = blockVerticalPaddingDp(metrics, block.short) + titleLineHeightDp(metrics, compact)
    if (plan.pillLabel != null) {
        floor += pillHeightDp(metrics) + pillGapDp()
    }
    if (plan.showMetaRow) {
        floor += metaRowHeightDp(metrics)
    } else if (plan.showDurationLine) {
        floor += scaledLineDp(16f, metrics.fontScale)
    }
    floor = floor.coerceAtLeast(minBlockHeightDp(block, metrics))
    return durationToHeight(block.durationMin).coerceAtLeast(floor)
}