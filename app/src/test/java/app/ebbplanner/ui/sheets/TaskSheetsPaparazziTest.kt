package app.ebbplanner.ui.sheets

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import org.junit.Rule
import org.junit.Test

class TaskSheetsPaparazziTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_6.copy(fontScale = 1f),
            renderingMode = SessionParams.RenderingMode.SHRINK,
        )

    @Test
    fun t5_reminder_picker_default_dark() {
        paparazzi.snapshot("t5_reminder_picker_default_dark") {
            TaskSheetsAppPreview(darkTheme = true) { ReminderPickerDesign() }
        }
    }

    @Test
    fun t6_repeat_editor_presets_dark() {
        paparazzi.snapshot("t6_repeat_editor_presets_dark") {
            TaskSheetsAppPreview(darkTheme = true) { RepeatEditorDesign() }
        }
    }

    @Test
    fun t7_break_it_down_empty_dark() {
        paparazzi.snapshot("t7_break_it_down_empty_dark") {
            TaskSheetsAppPreview(darkTheme = true) { BreakItDownDesign() }
        }
    }

    @Test
    fun t8_schedule_picker_default_dark() {
        paparazzi.snapshot("t8_schedule_picker_default_dark") {
            TaskSheetsAppPreview(darkTheme = true) { SchedulePickerDesign() }
        }
    }
}
