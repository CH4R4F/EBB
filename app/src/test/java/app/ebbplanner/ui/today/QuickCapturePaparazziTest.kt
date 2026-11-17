package app.ebbplanner.ui.today

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import org.junit.Rule
import org.junit.Test

class QuickCapturePaparazziTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_6.copy(fontScale = 1f),
            renderingMode = SessionParams.RenderingMode.SHRINK,
        )

    private val fixtures = quickCaptureFixtures()

    private fun snap(
        name: String,
        dark: Boolean,
        capture: QuickCapturePresentationState,
    ) {
        paparazzi.snapshot(name = name) {
            QuickCaptureAppPreview(darkTheme = dark, capture = capture)
        }
    }

    @Test fun t1_quick_capture_empty_dark() = snap("t1_quick_capture_empty_dark", true, fixtures.getValue("empty"))

    @Test
    fun t1_quick_capture_typing_dark() = snap("t1_quick_capture_typing_dark", true, fixtures.getValue("typing"))

    @Test
    fun t1_quick_capture_counter_dark() = snap("t1_quick_capture_counter_dark", true, fixtures.getValue("counter"))

    @Test
    fun t1_quick_capture_draft_restored_dark() {
        snap("t1_quick_capture_draft_restored_dark", true, fixtures.getValue("draft"))
    }

    @Test
    fun t1_quick_capture_paste_prompt_dark() {
        snap("t1_quick_capture_paste_prompt_dark", true, fixtures.getValue("paste"))
    }

    @Test
    fun t1_quick_capture_nl_highlight_dark() {
        snap("t1_quick_capture_nl_highlight_dark", true, fixtures.getValue("nl"))
    }
}
