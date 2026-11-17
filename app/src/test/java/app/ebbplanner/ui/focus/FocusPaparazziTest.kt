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
    fun f1_focus_running_ring_dark() {
        paparazzi.snapshot("f1_focus_running_ring_dark") {
            FocusAppPreview(
                darkTheme = true,
                state =
                    FocusPresentationState(
                        mode = FocusVisualMode.RunningRing,
                        topBarLabel = "Countdown · until 11:15",
                        timerLabel = "33:12",
                    ),
            )
        }
    }

    @Test
    fun f1_focus_running_bar_dark() {
        paparazzi.snapshot("f1_focus_running_bar_dark") {
            FocusAppPreview(
                darkTheme = true,
                state =
                    FocusPresentationState(
                        mode = FocusVisualMode.RunningBar,
                        topBarLabel = "Countdown · until 11:15",
                        timerLabel = "33:12",
                    ),
            )
        }
    }

    @Test
    fun f2_times_up_in_app_dark() {
        paparazzi.snapshot("f2_times_up_in_app_dark") {
            FocusAppPreview(
                darkTheme = true,
                state =
                    FocusPresentationState(
                        mode = FocusVisualMode.TimesUp,
                        topBarLabel = "Fixed · 25 min",
                        runningTitle = "Emails",
                        title = "Emails",
                    ),
            )
        }
    }

    @Test
    fun f2_times_up_lockscreen_dark() {
        paparazzi.snapshot("f2_times_up_lockscreen_dark") {
            FocusAppPreview(
                darkTheme = true,
                state =
                    FocusPresentationState(
                        mode = FocusVisualMode.TimesUpLockscreen,
                        runningTitle = "Emails",
                    ),
            )
        }
    }

    @Test
    fun f3_hyperfocus_gentle_dark() {
        paparazzi.snapshot("f3_hyperfocus_gentle_dark") {
            FocusAppPreview(
                darkTheme = true,
                state =
                    FocusPresentationState(
                        mode = FocusVisualMode.Hyperfocus,
                        topBarLabel = "Countdown · until 11:15",
                        timerLabel = "48:12",
                    ),
            )
        }
    }

    @Test
    fun f4_break_ended_dark() {
        paparazzi.snapshot("f4_break_ended_dark") {
            FocusAppPreview(
                darkTheme = true,
                state = FocusPresentationState(mode = FocusVisualMode.BreakEnded, topBarLabel = "Break"),
            )
        }
    }
}
