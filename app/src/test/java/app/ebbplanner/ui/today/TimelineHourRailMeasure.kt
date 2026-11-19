package app.ebbplanner.ui.today

import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import org.robolectric.RuntimeEnvironment

fun robolectricTimelineHourRail(
    fontScale: Float,
    compactHourLabels: Boolean,
    is24Hour: Boolean = true,
): Dp {
    val context = RuntimeEnvironment.getApplication()
    val resolver = createFontFamilyResolver(context)
    val dm = context.resources.displayMetrics
    val density = Density(dm.density, fontScale)
    val textMeasurer =
        TextMeasurer(
            defaultFontFamilyResolver = resolver,
            defaultDensity = density,
            defaultLayoutDirection = LayoutDirection.Ltr,
        )
    return measureTimelineHourRailWidthDp(
        textMeasurer,
        density,
        fontScale,
        compactHourLabels,
        is24Hour,
    )
}

fun robolectricTimelineTrackWidth(
    screenWidthDp: Int,
    fontScale: Float,
    compactHourLabels: Boolean = false,
    is24Hour: Boolean = true,
): Dp {
    val rail = robolectricTimelineHourRail(fontScale, compactHourLabels, is24Hour)
    return timelineTrackWidthDp(screenWidthDp.dp, rail)
}