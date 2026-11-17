package app.ebbplanner.ui.week

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import org.junit.Rule
import org.junit.Test

class WeekPaparazziTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_6.copy(fontScale = 1f),
            renderingMode = SessionParams.RenderingMode.SHRINK,
        )

    @Test fun m3_week_portrait_dark() = snap("m3_week_portrait_dark", weekPortrait())

    @Test fun m3_week_empty_dark() = snap("m3_week_empty_dark", weekEmpty())

    @Test fun m3_week_drag_dark() = snap("m3_week_drag_dark", weekDrag())

    private fun snap(
        name: String,
        state: WeekPresentationState,
    ) {
        paparazzi.snapshot(name) { WeekAppPreview(darkTheme = true, state = state) }
    }
}

class WeekLandscapePaparazziTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig =
                DeviceConfig.PIXEL_6.copy(
                    screenWidth = 915,
                    screenHeight = 412,
                    fontScale = 1f,
                ),
            renderingMode = SessionParams.RenderingMode.SHRINK,
        )

    @Test
    fun m3_week_landscape_dark() {
        paparazzi.snapshot("m3_week_landscape_dark") {
            WeekAppPreview(darkTheme = true, state = weekLandscape())
        }
    }
}
