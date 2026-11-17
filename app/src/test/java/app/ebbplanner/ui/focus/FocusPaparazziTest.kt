package app.ebbplanner.ui.focus

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import org.junit.Rule
import org.junit.Test

class FocusPaparazziTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_6.copy(fontScale = 1f),
            renderingMode = SessionParams.RenderingMode.SHRINK,
        )

    @Test
    fun f1_focus_setup_dark() {
        paparazzi.snapshot("f1_focus_setup_dark") {
            FocusAppPreview(darkTheme = true, state = FocusPresentationState(mode = FocusVisualMode.Setup))
        }
    }

    @Test
    fun f2_times_up_in_app_dark() {
        paparazzi.snapshot("f2_times_up_in_app_dark") {
            FocusAppPreview(darkTheme = true, state = FocusPresentationState(mode = FocusVisualMode.TimesUp))
        }
    }

    @Test
    fun f3_hyperfocus_gentle_dark() {
        paparazzi.snapshot("f3_hyperfocus_gentle_dark") {
            FocusAppPreview(darkTheme = true, state = FocusPresentationState(mode = FocusVisualMode.Hyperfocus))
        }
    }

    @Test
    fun f4_break_ended_dark() {
        paparazzi.snapshot("f4_break_ended_dark") {
            FocusAppPreview(darkTheme = true, state = FocusPresentationState(mode = FocusVisualMode.BreakEnded))
        }
    }
}
