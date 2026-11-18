package app.ebbplanner.ui

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.ebbplanner.data.preferences.UserPreferencesRepository
import app.ebbplanner.domain.day.MakeItFitPlan
import app.ebbplanner.domain.day.FitMoveSuggestion
import app.ebbplanner.ui.daytools.MakeItFitDesign
import app.ebbplanner.ui.daytools.ShiftTheRestDesign
import app.ebbplanner.ui.onboarding.OnboardingAppPreview
import app.ebbplanner.ui.onboarding.OnboardingDayShapeScreen
import app.ebbplanner.ui.onboarding.OnboardingPreferencesScreen
import app.ebbplanner.ui.onboarding.OnboardingUiState
import app.ebbplanner.ui.taskdetail.TaskDetailAppPreview
import app.ebbplanner.ui.taskdetail.taskDetailExisting
import app.ebbplanner.ui.today.TodayAppPreview
import app.ebbplanner.ui.today.todayPopulatedDark
import app.ebbplanner.ui.week.WeekAppPreview
import app.ebbplanner.ui.week.weekPortrait
import com.android.ide.common.rendering.api.SessionParams
import org.junit.Rule
import org.junit.Test

class QaFixFontScalePaparazziTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_6.copy(fontScale = 2f),
            renderingMode = SessionParams.RenderingMode.SHRINK,
        )

    @Test
    fun qa_today_font_scale_2() = snap("qa_today_font_scale_2") { TodayAppPreview(darkTheme = true, state = todayPopulatedDark()) }

    @Test
    fun qa_week_font_scale_2() = snap("qa_week_font_scale_2") { WeekAppPreview(darkTheme = true, state = weekPortrait()) }

    @Test
    fun qa_onboarding_week_start_font_scale_2() =
        snap("qa_onboarding_week_start_font_scale_2") {
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
    fun qa_task_detail_font_scale_2() =
        snap("qa_task_detail_font_scale_2") {
            TaskDetailAppPreview(darkTheme = true, state = taskDetailExisting())
        }

    @Test
    fun qa_shift_sheet_font_scale_2() = snap("qa_shift_sheet_font_scale_2") { ShiftTheRestDesign(onApplyShift = {}, onCancel = {}) }

    @Test
    fun qa_make_it_fit_font_scale_2() =
        snap("qa_make_it_fit_font_scale_2") {
            MakeItFitDesign(
                plan =
                    MakeItFitPlan(
                        plannedMinutes = 18 * 60,
                        availableMinutes = 15 * 60,
                        afterPlannedMinutes = 14 * 60,
                        moves =
                            listOf(
                                FitMoveSuggestion(
                                    taskId = "late",
                                    title = "Late meeting",
                                    durationMin = 45,
                                    reason = "Move to inbox",
                                    selected = true,
                                ),
                            ),
                    ),
                onApply = {},
                onCancel = {},
            )
        }

    @Test
    fun qa_onboarding_day_shape_font_scale_2() =
        snap("qa_onboarding_day_shape_font_scale_2") {
            OnboardingAppPreview(darkTheme = true) {
                OnboardingDayShapeScreen(
                    state = OnboardingUiState(),
                    onWakeChange = {},
                    onBedChange = {},
                    onContinue = {},
                    onSkip = {},
                )
            }
        }

    private fun snap(
        name: String,
        content: @androidx.compose.runtime.Composable () -> Unit,
    ) {
        paparazzi.snapshot(name, content)
    }
}