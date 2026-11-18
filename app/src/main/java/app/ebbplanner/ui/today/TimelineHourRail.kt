package app.ebbplanner.ui.today

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
import kotlin.math.max

internal val timelineHourRailBase = 56.dp

private const val CHAR_WIDTH_EM = 0.58f

fun timelineHourRailWidthDp(
    metrics: TimelineLayoutMetrics,
    compactHourLabels: Boolean,
): Dp {
    val scale = metrics.fontScale.coerceIn(1f, 2.5f)
    val hourLabelSp = 12f * scale
    val hourChars = if (compactHourLabels) 2f else 5f
    val hourTextDp = hourChars * hourLabelSp * CHAR_WIDTH_EM + 8f
    val badgeSp = 11f * scale
    val badgeTextDp = 5f * badgeSp * CHAR_WIDTH_EM + 12f
    val measured = max(hourTextDp, badgeTextDp)
    return max(timelineHourRailBase.value, ceil(measured).toFloat()).dp
}

fun timelineTrackStartAfterRailDp(
    metrics: TimelineLayoutMetrics,
    compactHourLabels: Boolean,
): Dp = timelineHourRailWidthDp(metrics, compactHourLabels) + 4.dp

fun timelineTrackWidthDp(
    screenWidthDp: Dp,
    metrics: TimelineLayoutMetrics,
    compactHourLabels: Boolean,
    trackEnd: Dp = 8.dp,
): Dp =
    (screenWidthDp - timelineTrackStartAfterRailDp(metrics, compactHourLabels) - trackEnd).coerceAtLeast(120.dp)

fun nowBadgeHeightDp(metrics: TimelineLayoutMetrics): Dp =
    (16f * metrics.fontScale.coerceIn(1f, 2.5f) + 6f).dp

fun nowBadgeOverlapsHourLabel(
    nowY: Dp,
    hourY: Dp,
    badgeHeight: Dp,
): Boolean {
    val badgeTop = nowY - badgeHeight / 2
    val badgeBottom = nowY + badgeHeight / 2
    val labelTop = hourY - 2.dp
    val labelBottom = hourY + 18.dp
    return badgeBottom > labelTop && badgeTop < labelBottom
}