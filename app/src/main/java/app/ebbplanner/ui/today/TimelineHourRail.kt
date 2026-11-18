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
import kotlin.math.ceil
import kotlin.math.max

internal val timelineHourRailBase = 56.dp

private const val BADGE_SURFACE_INSET_DP = 4f
private const val BADGE_START_PADDING_DP = 2f
private const val BADGE_TEXT_HORIZONTAL_PADDING_DP = 12f
private const val BADGE_RAIL_EXTRA_DP = 6f

private val badgeWidthProbeTimes = listOf("00:00", "20:00", "88:88")

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
): Dp =
    badgeWidthProbeTimes.maxOf { sample ->
        measureTimelineTextWidthDp(textMeasurer, density, sample, style)
    }

fun measureTimelineHourRailWidthDp(
    textMeasurer: TextMeasurer,
    density: Density,
    fontScale: Float,
    compactHourLabels: Boolean,
): Dp {
    val badgeStyle = timelineNowBadgeTextStyle()
    val hourStyle = timelineHourLabelTextStyle()
    val scaledDensity = Density(density.density, fontScale)

    val badgeTextWidth = maxBadgeTextWidthDp(textMeasurer, scaledDensity, badgeStyle)
    val badgeRail =
        badgeTextWidth +
            BADGE_TEXT_HORIZONTAL_PADDING_DP.dp +
            BADGE_RAIL_EXTRA_DP.dp +
            BADGE_SURFACE_INSET_DP.dp +
            BADGE_START_PADDING_DP.dp

    val hourSamples =
        if (compactHourLabels) {
            listOf("9", "12", "88")
        } else {
            listOf("9:00", "12:00", "00:00")
        }
    val hourTextWidth =
        hourSamples.maxOf { sample ->
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
): Dp {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    return remember(textMeasurer, density, compactHourLabels, fontScale) {
        measureTimelineHourRailWidthDp(textMeasurer, density, fontScale, compactHourLabels)
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