package app.ebbplanner.ui.coloricon

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import org.junit.Rule
import org.junit.Test

class ColorIconPaparazziTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_6.copy(fontScale = 1f),
            renderingMode = SessionParams.RenderingMode.SHRINK,
        )

    @Test fun t4_color_icon_selected_dark() = snap("t4_color_icon_selected_dark", colorIconSelected())

    @Test fun t4_color_icon_search_empty_dark() = snap("t4_color_icon_search_empty_dark", colorIconSearchEmpty())

    private fun snap(
        name: String,
        state: ColorIconPresentationState,
    ) {
        paparazzi.snapshot(name) { ColorIconAppPreview(darkTheme = true, state = state) }
    }
}
