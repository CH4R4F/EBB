package app.ebbplanner.ui.routines

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import org.junit.Rule
import org.junit.Test

class RoutineFlowPaparazziTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_6.copy(fontScale = 1f),
            renderingMode = SessionParams.RenderingMode.SHRINK,
        )

    @Test
    fun r1_routine_editor_editing_dark() {
        paparazzi.snapshot("r1_routine_editor_editing_dark") {
            RoutineFlowAppPreview(darkTheme = true) { RoutineEditorEditingDesign() }
        }
    }

    @Test
    fun r2_routine_run_first_dark() {
        paparazzi.snapshot("r2_routine_run_first_dark") {
            RoutineFlowAppPreview(darkTheme = true) { RoutineRunFirstDesign() }
        }
    }

    @Test
    fun r3_templates_gallery_dark() {
        paparazzi.snapshot("r3_templates_gallery_dark") {
            RoutineFlowAppPreview(darkTheme = true) { TemplatesGalleryDesign() }
        }
    }
}
