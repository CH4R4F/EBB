package app.ebbplanner.ui.routines

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import org.junit.Rule
import org.junit.Test

class RoutinesPaparazziTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_6.copy(fontScale = 1f),
            renderingMode = SessionParams.RenderingMode.SHRINK,
        )

    @Test fun m4_routines_empty_dark() = snap("m4_routines_empty_dark", routinesEmpty())

    @Test fun m4_routines_populated_dark() = snap("m4_routines_populated_dark", routinesPopulated())

    @Test fun m4_routines_running_dark() = snap("m4_routines_running_dark", routinesRunning())

    private fun snap(
        name: String,
        state: RoutinesPresentationState,
    ) {
        paparazzi.snapshot(name) { RoutinesAppPreview(darkTheme = true, state = state) }
    }
}
