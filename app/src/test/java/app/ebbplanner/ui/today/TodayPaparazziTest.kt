package app.ebbplanner.ui.today

import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import org.junit.Rule
import org.junit.Test

class TodayPaparazziTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_6.copy(fontScale = 1f),
            renderingMode = SessionParams.RenderingMode.SHRINK,
        )

    private fun snap(
        name: String,
        dark: Boolean,
        state: TodayPresentationState,
        rtl: Boolean = false,
    ) {
        paparazzi.snapshot(name = name) {
            val direction = if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr
            androidx.compose.runtime.CompositionLocalProvider(LocalLayoutDirection provides direction) {
                TodayAppPreview(darkTheme = dark, state = state)
            }
        }
    }

    @Test fun m1_today_populated_dark() = snap("m1_today_populated_dark", true, todayPopulatedDark())

    @Test fun m1_today_populated_light() = snap("m1_today_populated_light", false, todayPopulatedLight())

    @Test fun m1_today_empty_dark() = snap("m1_today_empty_dark", true, todayEmpty())

    @Test fun m1_today_loading_dark() = snap("m1_today_loading_dark", true, todayLoading())

    @Test fun m1_today_drag_dark() = snap("m1_today_drag_dark", true, todayDrag())

    @Test fun m1_today_multiselect_dark() = snap("m1_today_multiselect_dark", true, todayMultiselect())

    @Test fun m1_today_reminders_banner_dark() = snap("m1_today_reminders_banner_dark", true, todayRemindersBanner())

    @Test fun m1_today_overfull_dark() = snap("m1_today_overfull_dark", true, todayOverfull())

    @Test fun m1_today_overlap_dark() = snap("m1_today_overlap_dark", true, todayOverlap())

    @Test fun m1_today_dense_dark() = snap("m1_today_dense_dark", true, todayDense())

    @Test fun m1_today_past_day_dark() = snap("m1_today_past_day_dark", true, todayPastDay())

    @Test fun m1_today_future_day_dark() = snap("m1_today_future_day_dark", true, todayFutureDay())

    @Test fun m1_today_after_bedtime_dark() = snap("m1_today_after_bedtime_dark", true, todayAfterBedtime())

    @Test fun m1_today_rtl_dark() = snap("m1_today_rtl_dark", true, todayPopulatedDark(), rtl = true)
}

class TodayPaparazziFont200Test {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_6.copy(fontScale = 2f),
            renderingMode = SessionParams.RenderingMode.SHRINK,
        )

    @Test
    fun m1_today_font200_dark() {
        paparazzi.snapshot("m1_today_font200_dark") {
            TodayAppPreview(darkTheme = true, state = todayCompactTypography())
        }
    }
}
