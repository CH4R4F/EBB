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
    fun t2_task_detail_new_dark() {
        paparazzi.snapshot("t2_task_detail_new_dark") {
            TaskDetailAppPreview(darkTheme = true, state = taskDetailNew())
        }
    }

    @Test
    fun t2_task_detail_done_dark() {
        paparazzi.snapshot("t2_task_detail_done_dark") {
            TaskDetailAppPreview(darkTheme = true, state = taskDetailDone())
        }
    }

    @Test
    fun t2_task_detail_recurring_dark() {
        paparazzi.snapshot("t2_task_detail_recurring_dark") {
            TaskDetailAppPreview(darkTheme = true, state = taskDetailRecurring())
        }
    }

    @Test
    fun t2_task_detail_many_subtasks_dark() {
        paparazzi.snapshot("t2_task_detail_many_subtasks_dark") {
            TaskDetailAppPreview(darkTheme = true, state = taskDetailManySubtasks())
        }
    }

    @Test
    fun t3_estimate_picker_presets_dark() {
        paparazzi.snapshot("t3_estimate_picker_presets_dark") {
            EstimatePickerAppPreview(darkTheme = true, state = estimatePickerPresets())
        }
    }

    @Test
    fun t3_estimate_picker_custom_dark() {
        paparazzi.snapshot("t3_estimate_picker_custom_dark") {
            EstimatePickerAppPreview(darkTheme = true, state = estimatePickerCustom())
        }
    }

    @Test
    fun t3_estimate_picker_big_prompt_dark() {
        paparazzi.snapshot("t3_estimate_picker_big_prompt_dark") {
            EstimatePickerAppPreview(darkTheme = true, state = estimatePickerBigPrompt())
        }
    }
}
