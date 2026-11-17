package app.ebbplanner.ui.search

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import org.junit.Rule
import org.junit.Test

class SearchPaparazziTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_6.copy(fontScale = 1f),
            renderingMode = SessionParams.RenderingMode.SHRINK,
        )

    @Test fun m5_search_recent_dark() = snap("m5_search_recent_dark", searchRecent())

    @Test fun m5_search_results_dark() = snap("m5_search_results_dark", searchResults())

    @Test fun m5_search_no_results_dark() = snap("m5_search_no_results_dark", searchNoResults())

    private fun snap(
        name: String,
        state: SearchPresentationState,
    ) {
        paparazzi.snapshot(name) { SearchAppPreview(darkTheme = true, state = state) }
    }
}
