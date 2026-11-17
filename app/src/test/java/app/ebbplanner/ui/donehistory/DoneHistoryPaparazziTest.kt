package app.ebbplanner.ui.donehistory

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import org.junit.Rule
import org.junit.Test

class DoneHistoryPaparazziTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_6.copy(fontScale = 1f),
            renderingMode = SessionParams.RenderingMode.SHRINK,
        )

    @Test fun m6_done_history_empty_dark() = snap("m6_done_history_empty_dark", doneHistoryEmpty())

    @Test fun m6_done_history_populated_dark() = snap("m6_done_history_populated_dark", doneHistoryPopulated())

    private fun snap(
        name: String,
        state: DoneHistoryPresentationState,
    ) {
        paparazzi.snapshot(name) { DoneHistoryAppPreview(darkTheme = true, state = state) }
    }
}
