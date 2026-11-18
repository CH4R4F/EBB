package app.ebbplanner.data.preferences

import app.ebbplanner.domain.time.WeekStart
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.util.Locale

class WeekStartPreferenceTest {
    @Test
    fun localeDefault_matchesWeekFields() {
        val resolved = WeekStart.resolveDayOfWeek(UserPreferencesRepository.WEEK_START_USE_LOCALE, Locale.US)
        assertEquals(java.time.temporal.WeekFields.of(Locale.US).firstDayOfWeek, resolved)
    }

    @Test
    fun storedSunday_usesSunday() {
        assertEquals(DayOfWeek.SUNDAY, WeekStart.resolveDayOfWeek(DayOfWeek.SUNDAY.value))
    }
}