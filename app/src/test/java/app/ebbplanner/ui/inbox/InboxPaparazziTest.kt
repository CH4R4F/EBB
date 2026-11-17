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

    @Test
    fun m2_inbox_populated_dark() {
        paparazzi.snapshot("m2_inbox_populated_dark") {
            InboxAppPreview(darkTheme = true, state = inboxPopulated())
        }
    }
}
