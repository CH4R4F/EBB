package app.ebbplanner.ui.daytools

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import org.junit.Rule
import org.junit.Test

class DayToolsPaparazziTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_6.copy(fontScale = 1f),
            renderingMode = SessionParams.RenderingMode.SHRINK,
        )

    @Test
    fun d1_shift_the_rest_default_dark() {
        paparazzi.snapshot("d1_shift_the_rest_default_dark") {
            DayToolsAppPreview(darkTheme = true) { ShiftTheRestDesign() }
        }
    }

    @Test
    fun d2_overlap_helper_default_dark() {
        paparazzi.snapshot("d2_overlap_helper_default_dark") {
            DayToolsAppPreview(darkTheme = true) { OverlapHelperDesign() }
        }
    }

    @Test
    fun d3_make_it_fit_default_dark() {
        paparazzi.snapshot("d3_make_it_fit_default_dark") {
            DayToolsAppPreview(darkTheme = true) { MakeItFitDesign() }
        }
    }

    @Test
    fun d4_plan_my_day_step1_dark() {
        paparazzi.snapshot("d4_plan_my_day_step1_dark") {
            DayToolsAppPreview(darkTheme = true) { PlanMyDayDesign() }
        }
    }

    @Test
    fun d5_end_of_day_review_step1_dark() {
        paparazzi.snapshot("d5_end_of_day_review_step1_dark") {
            DayToolsAppPreview(darkTheme = true) { EndOfDayReviewDesign() }
        }
    }

    @Test
    fun d6_welcome_back_default_dark() {
        paparazzi.snapshot("d6_welcome_back_default_dark") {
            DayToolsAppPreview(darkTheme = true) { WelcomeBackDesign() }
        }
    }

    @Test
    fun d7_event_detail_default_dark() {
        paparazzi.snapshot("d7_event_detail_default_dark") {
            DayToolsAppPreview(darkTheme = true) { EventDetailDesign() }
        }
    }

    @Test
    fun d8_jump_to_date_default_dark() {
        paparazzi.snapshot("d8_jump_to_date_default_dark") {
            DayToolsAppPreview(darkTheme = true) { JumpToDateDesign() }
        }
    }
}
