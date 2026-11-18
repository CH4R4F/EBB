package app.ebbplanner.ui.week

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test

private val weekRangeNov =
    formatWeekRangeLabel(
        LocalDate.of(2025, 11, 14),
        LocalDate.of(2025, 11, 20),
    )

class WeekRangeHeaderF100W411PaparazziTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_6.copy(fontScale = 1f),
            renderingMode = SessionParams.RenderingMode.SHRINK,
        )

    @Test
    fun week_range_f100_w411() = paparazzi.snapWeekRange("week_range_f100_w411")
}

class WeekRangeHeaderF200W411PaparazziTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_6.copy(fontScale = 2f),
            renderingMode = SessionParams.RenderingMode.SHRINK,
        )

    @Test
    fun week_range_f200_w411() = paparazzi.snapWeekRange("week_range_f200_w411")
}

class WeekRangeHeaderF100W360PaparazziTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_6.copy(screenWidth = 360, fontScale = 1f),
            renderingMode = SessionParams.RenderingMode.SHRINK,
        )

    @Test
    fun week_range_f100_w360() = paparazzi.snapWeekRange("week_range_f100_w360")
}

class WeekRangeHeaderF200W360PaparazziTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_6.copy(screenWidth = 360, fontScale = 2f),
            renderingMode = SessionParams.RenderingMode.SHRINK,
        )

    @Test
    fun week_range_f200_w360() = paparazzi.snapWeekRange("week_range_f200_w360")
}

private fun Paparazzi.snapWeekRange(name: String) {
    snapshot(name) {
        WeekAppPreview(
            darkTheme = true,
            state = weekPortrait().copy(rangeLabel = weekRangeNov),
        )
    }
}