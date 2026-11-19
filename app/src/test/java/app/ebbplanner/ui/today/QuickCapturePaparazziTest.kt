@file:Suppress("MaxLineLength", "TooManyFunctions", "FunctionNaming")

package app.ebbplanner.ui.today

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.ebbplanner.ui.WithFontScale
import app.ebbplanner.ui.components.EbbSheetActions
import app.ebbplanner.ui.onboarding.OnboardingAppPreview
import com.android.ide.common.rendering.api.SessionParams
import org.junit.Rule
import org.junit.Test

class QuickCapturePaparazziTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_6.copy(fontScale = 1f),
            renderingMode = SessionParams.RenderingMode.SHRINK,
        )

    private val fixtures = quickCaptureFixtures()

    private fun snapOverlay(
        name: String,
        dark: Boolean,
        capture: QuickCapturePresentationState,
    ) {
        paparazzi.snapshot(name = name) {
            QuickCaptureAppPreview(darkTheme = dark, capture = capture)
        }
    }

    private fun snapStandalone(
        name: String,
        capture: QuickCapturePresentationState,
    ) {
        paparazzi.snapshot(name = name) {
            QuickCaptureStandalonePreview(darkTheme = true, capture = capture)
        }
    }

    @Test fun t1_quick_capture_empty_dark() = snapOverlay("t1_quick_capture_empty_dark", true, fixtures.getValue("empty"))

    @Test
    fun t1_quick_capture_typing_dark() = snapOverlay("t1_quick_capture_typing_dark", true, fixtures.getValue("typing"))

    @Test
    fun t1_quick_capture_counter_dark() = snapOverlay("t1_quick_capture_counter_dark", true, fixtures.getValue("counter"))

    @Test
    fun t1_quick_capture_draft_restored_dark() {
        snapOverlay("t1_quick_capture_draft_restored_dark", true, fixtures.getValue("draft"))
    }

    @Test
    fun t1_quick_capture_paste_prompt_dark() {
        snapOverlay("t1_quick_capture_paste_prompt_dark", true, fixtures.getValue("paste"))
    }

    @Test
    fun t1_quick_capture_nl_highlight_dark() {
        snapOverlay("t1_quick_capture_nl_highlight_dark", true, fixtures.getValue("nl"))
    }

    @Test
    fun quick_capture_empty() = snapStandalone("quick_capture_empty", fixtures.getValue("empty"))

    @Test
    fun quick_capture_parsed() = snapStandalone("quick_capture_parsed", fixtures.getValue("parsed"))

    @Test
    fun quick_capture_time_set() = snapStandalone("quick_capture_time_set", fixtures.getValue("timeSet"))
}

/** PIXEL_6 uses 945px width at 2.625 density → 360dp, not 360px. */
private fun quickCaptureDeviceW360Dp(fontScale: Float): DeviceConfig {
    val base = DeviceConfig.PIXEL_6
    val density = base.screenWidth / 411f
    return base.copy(
        screenWidth = (360 * density).toInt(),
        screenHeight = (800 * density).toInt(),
        fontScale = fontScale,
    )
}

private fun quickCapture360Paparazzi(
    fontScale: Float,
    renderingMode: SessionParams.RenderingMode = SessionParams.RenderingMode.SHRINK,
) = Paparazzi(
    deviceConfig = quickCaptureDeviceW360Dp(fontScale),
    renderingMode = renderingMode,
    maxPercentDifference = 0.02,
)

class QuickCaptureActionsF100W360PaparazziTest {
    @get:Rule
    val paparazzi = quickCapture360Paparazzi(1f, SessionParams.RenderingMode.NORMAL)

    @Test
    fun quick_capture_actions_f100_w360() = paparazzi.snapActions(1f)
}

class QuickCaptureF100W360PaparazziTest {
    @get:Rule
    val paparazzi = quickCapture360Paparazzi(1f)

    @Test
    fun quick_capture_empty_f100_w360() = paparazzi.snapScaled(1f, "empty")

    @Test
    fun quick_capture_parsed_f100_w360() = paparazzi.snapScaled(1f, "parsed")

    @Test
    fun quick_capture_time_set_f100_w360() = paparazzi.snapScaled(1f, "timeSet")
}

class QuickCaptureActionsF130W360PaparazziTest {
    @get:Rule
    val paparazzi = quickCapture360Paparazzi(1.3f, SessionParams.RenderingMode.NORMAL)

    @Test
    fun quick_capture_actions_f130_w360() = paparazzi.snapActions(1.3f)
}

class QuickCaptureF130W360PaparazziTest {
    @get:Rule
    val paparazzi = quickCapture360Paparazzi(1.3f)

    @Test
    fun quick_capture_empty_f130_w360() = paparazzi.snapScaled(1.3f, "empty")

    @Test
    fun quick_capture_parsed_f130_w360() = paparazzi.snapScaled(1.3f, "parsed")

    @Test
    fun quick_capture_time_set_f130_w360() = paparazzi.snapScaled(1.3f, "timeSet")
}

class QuickCaptureActionsF200W360PaparazziTest {
    @get:Rule
    val paparazzi = quickCapture360Paparazzi(2f, SessionParams.RenderingMode.NORMAL)

    @Test
    fun quick_capture_actions_f200_w360() = paparazzi.snapActions(2f)
}

class QuickCaptureF200W360PaparazziTest {
    @get:Rule
    val paparazzi = quickCapture360Paparazzi(2f)

    @Test
    fun quick_capture_empty_f200_w360() = paparazzi.snapScaled(2f, "empty")

    @Test
    fun quick_capture_parsed_f200_w360() = paparazzi.snapScaled(2f, "parsed")

    @Test
    fun quick_capture_time_set_f200_w360() = paparazzi.snapScaled(2f, "timeSet")
}

private fun Paparazzi.snapScaled(
    fontScale: Float,
    fixture: String,
) {
    val capture = quickCaptureFixtures().getValue(fixture)
    snapshot {
        WithFontScale(fontScale) {
            QuickCaptureStandalonePreview(darkTheme = true, capture = capture)
        }
    }
}

private fun Paparazzi.snapActions(fontScale: Float) {
    snapshot {
        WithFontScale(fontScale) {
            OnboardingAppPreview(darkTheme = true) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    EbbSheetActions(
                        confirmLabel = "Save",
                        onConfirm = {},
                        onCancel = {},
                    )
                }
            }
        }
    }
}
