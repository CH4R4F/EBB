package app.ebbplanner.ui.taskdetail

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import org.junit.Rule
import org.junit.Test

class TaskDetailPaparazziTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_6.copy(fontScale = 1f),
            renderingMode = SessionParams.RenderingMode.SHRINK,
        )

    @Test
    fun t2_task_detail_existing_dark() {
        paparazzi.snapshot("t2_task_detail_existing_dark") {
            TaskDetailAppPreview(darkTheme = true, state = taskDetailExisting())
        }
    }

    @Test
    fun t3_estimate_picker_dark() {
        paparazzi.snapshot("t3_estimate_picker_dark") {
            app.ebbplanner.ui.onboarding.OnboardingAppPreview(darkTheme = true) {
                EstimatePickerDesign(estimatePickerDefault())
            }
        }
    }
}
