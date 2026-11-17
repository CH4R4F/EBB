package app.ebbplanner.ui.garden

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import org.junit.Rule
import org.junit.Test

class GardenPaparazziTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_6.copy(fontScale = 1f),
            renderingMode = SessionParams.RenderingMode.SHRINK,
        )

    @Test
    fun g1_garden_wins_mature_dark() {
        paparazzi.snapshot("g1_garden_wins_mature_dark") {
            GardenAppPreview(darkTheme = true) { GardenWinsMatureDesign() }
        }
    }

    @Test
    fun g2_celebration_small_calm_dark() {
        paparazzi.snapshot("g2_celebration_small_calm_dark") {
            GardenAppPreview(darkTheme = true) { CelebrationSmallCalmDesign() }
        }
    }
}
