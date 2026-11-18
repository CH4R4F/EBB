package app.ebbplanner.ui

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.ebbplanner.data.preferences.UserPreferencesRepository
import app.ebbplanner.ui.daytools.MakeItFitDesign
import app.ebbplanner.ui.daytools.OverlapHelperDesign
import app.ebbplanner.ui.daytools.ShiftTheRestDesign
import app.ebbplanner.ui.inbox.InboxAppPreview
import app.ebbplanner.ui.inbox.inboxPopulated
import app.ebbplanner.ui.onboarding.OnboardingAppPreview
import app.ebbplanner.ui.onboarding.OnboardingPreferencesScreen
import app.ebbplanner.ui.onboarding.OnboardingUiState
import app.ebbplanner.ui.routines.RoutinesAppPreview
import app.ebbplanner.ui.routines.routinesPopulated
import app.ebbplanner.ui.settings.SettingsPreviewPanel
import app.ebbplanner.ui.settings.SettingsUiState
import app.ebbplanner.ui.today.QuickCaptureStandalonePreview
import app.ebbplanner.ui.today.TodayAnytimeChip
import app.ebbplanner.ui.today.TodayAnytimeTray
import app.ebbplanner.ui.today.TodayAppPreview
import app.ebbplanner.ui.today.quickCaptureFixtures
import app.ebbplanner.ui.today.todayPopulatedDark
import app.ebbplanner.ui.week.WeekAppPreview
import app.ebbplanner.ui.week.weekPortrait
import com.android.ide.common.rendering.api.SessionParams
import org.junit.Rule
import org.junit.Test

class QaFixScreenshotsPaparazziTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_6.copy(fontScale = 1f),
            renderingMode = SessionParams.RenderingMode.SHRINK,
        )

    @Test
    fun qa_today() = snap("qa_today") { TodayAppPreview(darkTheme = true, state = todayPopulatedDark()) }

    @Test
    fun qa_inbox() = snap("qa_inbox") { InboxAppPreview(darkTheme = true, state = inboxPopulated()) }

    @Test
    fun qa_week() = snap("qa_week") { WeekAppPreview(darkTheme = true, state = weekPortrait()) }

    @Test
    fun qa_routines() = snap("qa_routines") { RoutinesAppPreview(darkTheme = true, state = routinesPopulated()) }

    @Test
    fun qa_settings() =
        snap("qa_settings") {
            OnboardingAppPreview(darkTheme = true) {
                SettingsPreviewPanel(SettingsUiState(weekStartLabel = "Monday"))
            }
        }

    @Test
    fun qa_onboarding_week_start() =
        snap("qa_onboarding_week_start") {
            OnboardingAppPreview(darkTheme = true) {
                OnboardingPreferencesScreen(
                    state = OnboardingUiState(weekStartDay = UserPreferencesRepository.WEEK_START_USE_LOCALE),
                    onBreatherSelect = {},
                    onCelebrationSelect = {},
                    onWeekStartSelect = {},
                    onContinue = {},
                    onSkip = {},
                )
            }
        }

    @Test
    fun qa_quick_capture() =
        snap("qa_quick_capture") {
            QuickCaptureStandalonePreview(darkTheme = true, capture = quickCaptureFixtures().getValue("typing"))
        }

    @Test
    fun qa_anytime_chip() =
        snap("qa_anytime_chip") {
            OnboardingAppPreview(darkTheme = true) {
                TodayAnytimeTray(
                    chips = listOf(TodayAnytimeChip("1", "Water plants")),
                    onAddAnytime = {},
                )
            }
        }

    @Test
    fun qa_shift_sheet() =
        snap("qa_shift_sheet") {
            ShiftTheRestDesign(onApplyShift = {}, onCancel = {})
        }

    @Test
    fun qa_overlap_sheet() =
        snap("qa_overlap_sheet") {
            OverlapHelperDesign(onApply = {}, onCancel = {})
        }

    @Test
    fun qa_make_it_fit_sheet() =
        snap("qa_make_it_fit_sheet") {
            MakeItFitDesign(onApply = {}, onCancel = {})
        }

    private fun snap(
        name: String,
        content: @androidx.compose.runtime.Composable () -> Unit,
    ) {
        paparazzi.snapshot(name, content)
    }
}