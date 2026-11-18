package app.ebbplanner.domain.time

import app.ebbplanner.data.preferences.UserPreferencesRepository
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class WeekAnchorTest {
    @Test
    fun mondayStart_wednesdayToday_startsPreviousMonday() {
        val today = LocalDate.of(2025, 11, 19) // Wednesday
        val anchor = WeekAnchor.weekStartForPage(today, DayOfWeek.MONDAY.value, 0)
        assertEquals(LocalDate.of(2025, 11, 17), anchor)
    }

    @Test
    fun sundayStart_wednesdayToday_startsPreviousSunday() {
        val today = LocalDate.of(2025, 11, 19)
        val anchor = WeekAnchor.weekStartForPage(today, DayOfWeek.SUNDAY.value, 0)
        assertEquals(LocalDate.of(2025, 11, 16), anchor)
    }

    @Test
    fun localeDefault_pageOffset_preservesUserNavigation() {
        val today = LocalDate.of(2025, 11, 19)
        val thisWeek = WeekAnchor.weekStartForPage(today, UserPreferencesRepository.WEEK_START_USE_LOCALE, 0)
        val nextWeek = WeekAnchor.weekStartForPage(today, UserPreferencesRepository.WEEK_START_USE_LOCALE, 1)
        assertEquals(thisWeek.plusWeeks(1), nextWeek)
    }

    @Test
    fun preferenceChange_pageZero_reanchorsToToday() {
        val today = LocalDate.of(2025, 11, 19)
        val mondayWeek = WeekAnchor.weekStartForPage(today, DayOfWeek.MONDAY.value, 0)
        val sundayWeek = WeekAnchor.weekStartForPage(today, DayOfWeek.SUNDAY.value, 0)
        assertEquals(LocalDate.of(2025, 11, 17), mondayWeek)
        assertEquals(LocalDate.of(2025, 11, 16), sundayWeek)
    }
}