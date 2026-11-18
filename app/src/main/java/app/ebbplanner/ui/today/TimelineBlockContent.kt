package app.ebbplanner.ui.today

import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.ebbplanner.ui.theme.EbbTypography

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

fun pillBottomInsetDp(): Dp = 4.dp

fun blockVerticalPaddingDp(
    metrics: TimelineLayoutMetrics,
    short: Boolean,
): Dp =
    when {
        short -> 0.dp
        metrics.largeText -> 8.dp
        else -> 12.dp
    }

fun timeLeftPillLabelCandidates(fullLabel: String): List<String> {
    val match = Regex("(\\d+)\\s*min\\s+left", RegexOption.IGNORE_CASE).find(fullLabel)
    if (match == null) return listOf(fullLabel)
    val minutes = match.groupValues[1]
    return listOf(fullLabel, "${minutes}m left", "${minutes}m")
}

fun pickTimeLeftPillLabel(
    textMeasurer: TextMeasurer,
    density: Density,
    fullLabel: String,
    maxTextWidth: Dp,
    style: TextStyle = EbbTypography.labelM,
): String {
    if (maxTextWidth <= 0.dp) return timeLeftPillLabelCandidates(fullLabel).last()
    val maxPx = with(density) { maxTextWidth.toPx().toInt().coerceAtLeast(0) }
    val candidates = timeLeftPillLabelCandidates(fullLabel)
    return candidates.firstOrNull { candidate ->
        val layout =
            textMeasurer.measure(
                text = candidate,
                style = style,
                constraints = Constraints(maxWidth = maxPx),
                maxLines = 1,
                density = density,
            )
        layout.lineCount == 1 && !layout.didOverflowWidth
    } ?: candidates.last()
}

internal val timeLeftPillHorizontalPadding = 16.dp

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
            pillHeightDp(metrics) + pillGapDp() + pillBottomInsetDp()
        } else {
            0.dp
        }
    val pillLabel = block.minutesLeftLabel

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
        floor += pillHeightDp(metrics) + pillGapDp() + pillBottomInsetDp()
    }
    if (plan.showMetaRow) {
        floor += metaRowHeightDp(metrics)
    } else if (plan.showDurationLine) {
        floor += scaledLineDp(16f, metrics.fontScale)
    }
    floor = floor.coerceAtLeast(minBlockHeightDp(block, metrics))
    return durationToHeight(block.durationMin).coerceAtLeast(floor)
}