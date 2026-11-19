package app.ebbplanner.ui.today

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbTypography
import app.ebbplanner.ui.time.ClockFormat
import kotlin.math.ceil
import kotlin.math.max

internal val timelineHourRailBase = 56.dp

private const val BADGE_SURFACE_INSET_DP = 4f
private const val BADGE_START_PADDING_DP = 2f
private const val BADGE_TEXT_HORIZONTAL_PADDING_DP = 12f
private const val BADGE_RAIL_EXTRA_DP = 6f

fun timelineBadgeLabelSamples(is24Hour: Boolean): List<String> =
    (0 until 24).flatMap { hour ->
        listOf(
            ClockFormat.minutesToClockCompact(hour * 60, is24Hour),
            ClockFormat.minutesToClockCompact(hour * 60 + 59, is24Hour),
        )
    }.distinct()

fun timelineHourLabelSamples(is24Hour: Boolean): List<String> =
    (0..23).flatMap { hour -> ClockFormat.hourGutterLabelCandidates(hour, is24Hour) }.distinct()

fun pickHourGutterLabel(
    textMeasurer: TextMeasurer,
    density: Density,
    hour: Int,
    is24Hour: Boolean,
    maxTextWidth: Dp,
    style: TextStyle = timelineHourLabelTextStyle(),
): String {
    val candidates = ClockFormat.hourGutterLabelCandidates(hour, is24Hour)
    if (maxTextWidth <= 0.dp) return candidates.last()
    val maxPx = with(density) { maxTextWidth.toPx() }
    return candidates.firstOrNull { candidate ->
        val layout =
            textMeasurer.measure(
                text = candidate,
                style = style,
                constraints = Constraints(maxWidth = maxPx.toInt().coerceAtLeast(0)),
                maxLines = 1,
                softWrap = false,
                density = density,
            )
        !layout.didOverflowWidth && !layout.isLineEllipsized(0)
    } ?: candidates.last()
}

fun timelineNowBadgeTextStyle(): TextStyle =
    EbbTypography.labelS.copy(fontFeatureSettings = "tnum")

fun timelineHourLabelTextStyle(): TextStyle =
    EbbTypography.labelM.copy(fontFeatureSettings = "tnum")

fun measureTimelineTextWidthDp(
    textMeasurer: TextMeasurer,
    density: Density,
    text: String,
    style: TextStyle,
): Dp {
    val layout =
        textMeasurer.measure(
            text = text,
            style = style,
            constraints = Constraints(maxWidth = Int.MAX_VALUE),
            density = density,
        )
    return with(density) { layout.size.width.toDp() }
}

fun maxBadgeTextWidthDp(
    textMeasurer: TextMeasurer,
    density: Density,
    style: TextStyle = timelineNowBadgeTextStyle(),
    is24Hour: Boolean = true,
): Dp =
    timelineBadgeLabelSamples(is24Hour).maxOf { sample ->
        measureTimelineTextWidthDp(textMeasurer, density, sample, style)
    }

@Suppress("UNUSED_PARAMETER")
fun measureTimelineHourRailWidthDp(
    textMeasurer: TextMeasurer,
    density: Density,
    fontScale: Float,
    compactHourLabels: Boolean,
    is24Hour: Boolean = true,
): Dp {
    val badgeStyle = timelineNowBadgeTextStyle()
    val hourStyle = timelineHourLabelTextStyle()
    val scaledDensity = Density(density.density, fontScale)

    val badgeTextWidth = maxBadgeTextWidthDp(textMeasurer, scaledDensity, badgeStyle, is24Hour)
    val badgeRail =
        badgeTextWidth +
            BADGE_TEXT_HORIZONTAL_PADDING_DP.dp +
            BADGE_RAIL_EXTRA_DP.dp +
            BADGE_SURFACE_INSET_DP.dp +
            BADGE_START_PADDING_DP.dp

    val hourTextWidth =
        timelineHourLabelSamples(is24Hour).maxOf { sample ->
            measureTimelineTextWidthDp(textMeasurer, scaledDensity, sample, hourStyle)
        }
    val hourRail = hourTextWidth + EbbSpacing.s1

    val measured = max(badgeRail.value, hourRail.value)
    return max(timelineHourRailBase.value, ceil(measured).toFloat()).dp
}

@Composable
fun rememberTimelineHourRailWidth(
    compactHourLabels: Boolean,
    fontScale: Float,
    is24Hour: Boolean = true,
): Dp {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    return remember(textMeasurer, density, compactHourLabels, fontScale, is24Hour) {
        measureTimelineHourRailWidthDp(textMeasurer, density, fontScale, compactHourLabels, is24Hour)
    }
}

fun timelineTrackStartAfterRailDp(hourRail: Dp): Dp = hourRail + 4.dp

fun timelineTrackWidthDp(
    screenWidthDp: Dp,
    hourRail: Dp,
    trackEnd: Dp = 8.dp,
): Dp = (screenWidthDp - timelineTrackStartAfterRailDp(hourRail) - trackEnd).coerceAtLeast(120.dp)

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