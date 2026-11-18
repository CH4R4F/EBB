package app.ebbplanner.ui.week

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test

private val weekRangeSameMonth =
    formatWeekRangeLabel(
        LocalDate.of(2025, 11, 14),
        LocalDate.of(2025, 11, 20),
    )

private val weekRangeCrossMonth =
    formatWeekRangeLabel(
        LocalDate.of(2025, 10, 31),
        LocalDate.of(2025, 11, 6),
    )

/** PIXEL_6 uses 945px width at 2.625 density → 360dp, not 360px. */
private fun weekRangeDeviceW360Dp(fontScale: Float): DeviceConfig {
    val base = DeviceConfig.PIXEL_6
    val density = base.screenWidth / 411f
    return base.copy(
        screenWidth = (360 * density).toInt(),
        screenHeight = (800 * density).toInt(),
        fontScale = fontScale,
    )
}

class WeekRangeHeaderF100W411PaparazziTest {
    @get:Rule
    val paparazzi = weekRangePaparazzi(DeviceConfig.PIXEL_6.copy(fontScale = 1f))

    @Test
    fun week_range_sameMonth_f100_w411() = paparazzi.snapWeekRange("week_range_sameMonth_f100_w411", weekRangeSameMonth)
}

class WeekRangeHeaderF200W411PaparazziTest {
    @get:Rule
    val paparazzi = weekRangePaparazzi(DeviceConfig.PIXEL_6.copy(fontScale = 2f))

    @Test
    fun week_range_crossMonth_f200_w411() = paparazzi.snapWeekRange("week_range_crossMonth_f200_w411", weekRangeCrossMonth)
}

class WeekRangeHeaderF100W360PaparazziTest {
    @get:Rule
    val paparazzi =
        weekRangePaparazzi(weekRangeDeviceW360Dp(fontScale = 1f))

    @Test
    fun week_range_sameMonth_f100_w360() = paparazzi.snapWeekRange("week_range_sameMonth_f100_w360", weekRangeSameMonth)
}

class WeekRangeHeaderF200W360PaparazziTest {
    @get:Rule
    val paparazzi =
        weekRangePaparazzi(weekRangeDeviceW360Dp(fontScale = 2f))

    @Test
    fun week_range_crossMonth_f200_w360() = paparazzi.snapWeekRange("week_range_crossMonth_f200_w360", weekRangeCrossMonth)
}

private fun weekRangePaparazzi(device: DeviceConfig): Paparazzi =
    Paparazzi(
        deviceConfig = device,
        renderingMode = SessionParams.RenderingMode.NORMAL,
        maxPercentDifference = 0.02,
    )

private fun Paparazzi.snapWeekRange(
    name: String,
    range: String,
) {
    snapshot(name) {
        WeekAppPreview(
            darkTheme = true,
            state = weekPortrait().copy(rangeLabel = range, mode = WeekVisualMode.Loading),
        )
    }
}