package app.ebbplanner.domain.time

import app.ebbplanner.data.preferences.UserPreferencesRepository
import java.time.DayOfWeek
import java.time.temporal.WeekFields
import java.util.Locale

object WeekStart {
    fun resolveDayOfWeek(stored: Int, locale: Locale = Locale.getDefault()): DayOfWeek =
        if (stored == UserPreferencesRepository.WEEK_START_USE_LOCALE) {
            WeekFields.of(locale).firstDayOfWeek
        } else {
            DayOfWeek.of(stored.coerceIn(DayOfWeek.MONDAY.value, DayOfWeek.SUNDAY.value))
        }
}