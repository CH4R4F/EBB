package app.ebbplanner.ui.onboarding

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.ebbplanner.ui.theme.EbbTheme
import com.android.ide.common.rendering.api.SessionParams
import org.junit.Rule
import org.junit.Test

class OnboardingPaparazziTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig =
                DeviceConfig.NEXUS_5.copy(
                    screenWidth = 412,
                    screenHeight = 915,
                ),
            renderingMode = SessionParams.RenderingMode.SHRINK,
        )

    private val defaultState = OnboardingUiState()

    @Test
    fun o1_welcome_light() {
        paparazzi.snapshot(name = "o1_welcome_light") {
            EbbTheme(darkTheme = false) {
                OnboardingWelcomeScreen(onGetStarted = {}, onRestoreBackup = {})
            }
        }
    }

    @Test
    fun o1_welcome_dark() {
        paparazzi.snapshot(name = "o1_welcome_dark") {
            EbbTheme(darkTheme = true) {
                OnboardingWelcomeScreen(onGetStarted = {}, onRestoreBackup = {})
            }
        }
    }

    @Test
    fun o2_promise_light() {
        paparazzi.snapshot(name = "o2_promise_light") {
            EbbTheme(darkTheme = false) {
                OnboardingPromiseScreen(onContinue = {})
            }
        }
    }

    @Test
    fun o2_promise_dark() {
        paparazzi.snapshot(name = "o2_promise_dark") {
            EbbTheme(darkTheme = true) {
                OnboardingPromiseScreen(onContinue = {})
            }
        }
    }

    @Test
    fun o3_day_shape_light() {
        paparazzi.snapshot(name = "o3_day_shape_light") {
            EbbTheme(darkTheme = false) {
                OnboardingDayShapeScreen(
                    state = defaultState,
                    onWakeChange = {},
                    onBedChange = {},
                    onContinue = {},
                    onSkip = {},
                )
            }
        }
    }

    @Test
    fun o3_day_shape_dark() {
        paparazzi.snapshot(name = "o3_day_shape_dark") {
            EbbTheme(darkTheme = true) {
                OnboardingDayShapeScreen(
                    state = defaultState,
                    onWakeChange = {},
                    onBedChange = {},
                    onContinue = {},
                    onSkip = {},
                )
            }
        }
    }

    @Test
    fun o4_preferences_light() {
        paparazzi.snapshot(name = "o4_preferences_light") {
            EbbTheme(darkTheme = false) {
                OnboardingPreferencesScreen(
                    state = defaultState,
                    onBreatherSelect = {},
                    onCelebrationSelect = {},
                    onContinue = {},
                    onSkip = {},
                )
            }
        }
    }

    @Test
    fun o4_preferences_dark() {
        paparazzi.snapshot(name = "o4_preferences_dark") {
            EbbTheme(darkTheme = true) {
                OnboardingPreferencesScreen(
                    state = defaultState,
                    onBreatherSelect = {},
                    onCelebrationSelect = {},
                    onContinue = {},
                    onSkip = {},
                )
            }
        }
    }

    @Test
    fun o5_notifications_light() {
        paparazzi.snapshot(name = "o5_notifications_light") {
            EbbTheme(darkTheme = false) {
                OnboardingNotificationsScreen(onAllow = {}, onSkip = {})
            }
        }
    }

    @Test
    fun o5_notifications_dark() {
        paparazzi.snapshot(name = "o5_notifications_dark") {
            EbbTheme(darkTheme = true) {
                OnboardingNotificationsScreen(onAllow = {}, onSkip = {})
            }
        }
    }

    @Test
    fun o6_exact_alarms_light() {
        paparazzi.snapshot(name = "o6_exact_alarms_light") {
            EbbTheme(darkTheme = false) {
                OnboardingExactAlarmsScreen(onOpenSettings = {}, onSkip = {})
            }
        }
    }

    @Test
    fun o6_exact_alarms_dark() {
        paparazzi.snapshot(name = "o6_exact_alarms_dark") {
            EbbTheme(darkTheme = true) {
                OnboardingExactAlarmsScreen(onOpenSettings = {}, onSkip = {})
            }
        }
    }

    @Test
    fun o7_calendar_light() {
        paparazzi.snapshot(name = "o7_calendar_light") {
            EbbTheme(darkTheme = false) {
                OnboardingCalendarScreen(onAllow = {}, onSkip = {})
            }
        }
    }

    @Test
    fun o7_calendar_dark() {
        paparazzi.snapshot(name = "o7_calendar_dark") {
            EbbTheme(darkTheme = true) {
                OnboardingCalendarScreen(onAllow = {}, onSkip = {})
            }
        }
    }

    @Test
    fun o8_try_it_light() {
        paparazzi.snapshot(name = "o8_try_it_light") {
            EbbTheme(darkTheme = false) {
                OnboardingTryItScreen(onSkip = {}, onNext = {})
            }
        }
    }

    @Test
    fun o8_try_it_dark() {
        paparazzi.snapshot(name = "o8_try_it_dark") {
            EbbTheme(darkTheme = true) {
                OnboardingTryItScreen(onSkip = {}, onNext = {})
            }
        }
    }

    @Test
    fun o9_done_light() {
        paparazzi.snapshot(name = "o9_done_light") {
            EbbTheme(darkTheme = false) {
                OnboardingDoneScreen(
                    state = defaultState,
                    onOpenMyDay = {},
                    onSampleTasksChange = {},
                    onComplete = {},
                )
            }
        }
    }

    @Test
    fun o9_done_dark() {
        paparazzi.snapshot(name = "o9_done_dark") {
            EbbTheme(darkTheme = true) {
                OnboardingDoneScreen(
                    state = defaultState,
                    onOpenMyDay = {},
                    onSampleTasksChange = {},
                    onComplete = {},
                )
            }
        }
    }
}
