package app.ebbplanner.ui.inbox

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import org.junit.Rule
import org.junit.Test

class InboxPaparazziTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_6.copy(fontScale = 1f),
            renderingMode = SessionParams.RenderingMode.SHRINK,
        )

    @Test fun m2_inbox_populated_dark() = snap("m2_inbox_populated_dark", inboxPopulated())

    @Test fun m2_inbox_empty_dark() = snap("m2_inbox_empty_dark", inboxEmpty())

    @Test fun m2_inbox_filtered_empty_dark() = snap("m2_inbox_filtered_empty_dark", inboxFilteredEmpty())

    @Test fun m2_inbox_many_dark() = snap("m2_inbox_many_dark", inboxMany())

    @Test fun m2_inbox_loading_dark() = snap("m2_inbox_loading_dark", inboxLoading())

    @Test fun m2_inbox_multiselect_dark() = snap("m2_inbox_multiselect_dark", inboxMultiselect())

    @Test fun m2_inbox_swipe_left_dark() = snap("m2_inbox_swipe_left_dark", inboxSwipeLeft())

    @Test fun m2_inbox_swipe_right_dark() = snap("m2_inbox_swipe_right_dark", inboxSwipeRight())

    @Test fun m2_inbox_tidy_up_dark() = snap("m2_inbox_tidy_up_dark", inboxTidyUp())

    @Test fun m2_inbox_drag_to_today_dark() = snap("m2_inbox_drag_to_today_dark", inboxDragToToday())

    private fun snap(
        name: String,
        state: InboxPresentationState,
    ) {
        paparazzi.snapshot(name) {
            InboxAppPreview(darkTheme = true, state = state)
        }
    }
}
