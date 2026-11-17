package app.ebbplanner.ui.onboarding

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import org.junit.Rule
import org.junit.Test

/** Pixel 6 frame: 1080×2400 px at 420 dpi (~412×915 dp). fontScale must stay 1.0. */
class OnboardingPaparazziTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_6.copy(fontScale = 1f),
            renderingMode = SessionParams.RenderingMode.SHRINK,
        )

    private val stateDefault = OnboardingUiState()
    private val stateAfterMidnight = OnboardingUiState(wakeMinutes = 10 * 60, bedMinutes = 2 * 60)

    private fun snap(
        name: String,
        dark: Boolean,
        content: @androidx.compose.runtime.Composable () -> Unit,
    ) {
        paparazzi.snapshot(name = name) {
            OnboardingAppPreview(darkTheme = dark, content = content)
        }
    }

    @Test fun o1_welcome_light() = snap("o1_welcome_light", false) { OnboardingWelcomeScreen({}, {}) }

    @Test fun o1_welcome_dark() = snap("o1_welcome_dark", true) { OnboardingWelcomeScreen({}, {}) }

    @Test fun o2_promise_light() = snap("o2_promise_light", false) { OnboardingPromiseScreen {} }

    @Test fun o2_promise_dark() = snap("o2_promise_dark", true) { OnboardingPromiseScreen {} }

    @Test
    fun o3_day_shape_light() =
        snap("o3_day_shape_light", false) {
            OnboardingDayShapeScreen(stateDefault, {}, {}, {}, {})
        }

    @Test
    fun o3_day_shape_dark() =
        snap("o3_day_shape_dark", true) {
            OnboardingDayShapeScreen(stateDefault, {}, {}, {}, {})
        }

    @Test
    fun o3_day_shape_after_midnight_dark() =
        snap("o3_day_shape_after_midnight_dark", true) {
            OnboardingDayShapeScreen(stateAfterMidnight, {}, {}, {}, {})
        }

    @Test
    fun o4_preferences_dark() =
        snap("o4_preferences_dark", true) {
            OnboardingPreferencesScreen(stateDefault, {}, {}, {}, {})
        }

    @Test
    fun o8_try_it_step1_empty_dark() =
        snap("o8_try_it_step1_empty_dark", true) {
            OnboardingTryItScreen(step = TryItStep.Step1Empty, onSkip = {}, onNext = {})
        }

    @Test
    fun o8_try_it_step1_typed_dark() =
        snap("o8_try_it_step1_typed_dark", true) {
            OnboardingTryItScreen(
                step = TryItStep.Step1Typed,
                inputValue = "Buy stamps",
                onSkip = {},
                onNext = {},
            )
        }

    @Test
    fun o8_try_it_step2_drag_dark() =
        snap("o8_try_it_step2_drag_dark", true) {
            OnboardingTryItScreen(step = TryItStep.Step2Drag, onSkip = {}, onNext = {})
        }

    @Test
    fun o8_try_it_success_dark() =
        snap("o8_try_it_success_dark", true) {
            OnboardingTryItScreen(
                step = TryItStep.Success,
                celebrationBody = "Buy stamps is on today at 14:30.",
                onSkip = {},
                onNext = {},
            )
        }

    @Test
    fun o5_notifications_default_dark() =
        snap("o5_notifications_default_dark", true) {
            OnboardingNotificationsScreen(NotificationPrimerState.Default, {}, {})
        }

    @Test
    fun o5_notifications_granted_dark() =
        snap("o5_notifications_granted_dark", true) {
            OnboardingNotificationsScreen(NotificationPrimerState.Granted, {}, {})
        }

    @Test
    fun o5_notifications_denied_dark() =
        snap("o5_notifications_denied_dark", true) {
            OnboardingNotificationsScreen(NotificationPrimerState.Denied, {}, {})
        }

    @Test
    fun o6_exact_alarms_dark() = snap("o6_exact_alarms_dark", true) { OnboardingExactAlarmsScreen({}, {}) }

    @Test
    fun o7_calendar_default_dark() = snap("o7_calendar_default_dark", true) { OnboardingCalendarScreen({}, {}) }

    @Test
    fun o7_calendar_chooser_dark() = snap("o7_calendar_chooser_dark", true) { OnboardingCalendarChooserScreen {} }

    @Test
    fun o9_done_dark() =
        snap("o9_done_dark", true) {
            OnboardingDoneScreen(stateDefault, {}, {}, {})
        }
}
