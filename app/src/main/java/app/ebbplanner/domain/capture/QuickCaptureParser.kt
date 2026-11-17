@file:Suppress("LongMethod")

package app.ebbplanner.domain.capture

import app.ebbplanner.ui.today.QuickCaptureHighlight
import app.ebbplanner.ui.today.QuickCaptureHighlightKind
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale
import java.util.regex.Pattern

data class QuickCaptureParseResult(
    val title: String,
    val targetDate: LocalDate?,
    val startTime: LocalTime?,
    val durationMin: Int?,
    val highlights: List<QuickCaptureHighlight>,
)

object QuickCaptureParser {
    private val TIME_PATTERN =
        Pattern.compile(
            """(?i)\b(?:at\s+)?(\d{1,2})(?::(\d{2}))?\s*(am|pm)?\b""",
        )
    private val DURATION_PATTERN =
        Pattern.compile(
            """(?i)\b(\d+)\s*(?:h|hr|hrs|hour|hours)\b|\b(\d+)\s*(?:m|min|mins|minute|minutes)\b""",
        )
    private val TOMORROW_PATTERN = Pattern.compile("""(?i)\btomorrow\b""")

    fun parse(
        text: String,
        today: LocalDate = LocalDate.now(),
    ): QuickCaptureParseResult {
        val highlights = mutableListOf<QuickCaptureHighlight>()
        var targetDate: LocalDate? = null
        var startTime: LocalTime? = null
        var durationMin: Int? = null

        val tomorrowMatcher = TOMORROW_PATTERN.matcher(text)
        if (tomorrowMatcher.find()) {
            targetDate = today.plusDays(1)
            highlights +=
                QuickCaptureHighlight(
                    tomorrowMatcher.start(),
                    tomorrowMatcher.end(),
                    QuickCaptureHighlightKind.Tomorrow,
                )
        }

        val timeMatcher = TIME_PATTERN.matcher(text)
        if (timeMatcher.find()) {
            val hour = timeMatcher.group(1)?.toIntOrNull()
            val minute = timeMatcher.group(2)?.toIntOrNull() ?: 0
            val amPm = timeMatcher.group(3)?.lowercase(Locale.US)
            if (hour != null) {
                startTime = toLocalTime(hour, minute, amPm)
                highlights +=
                    QuickCaptureHighlight(
                        timeMatcher.start(),
                        timeMatcher.end(),
                        QuickCaptureHighlightKind.Time,
                    )
            }
        }

        val durationMatcher = DURATION_PATTERN.matcher(text)
        if (durationMatcher.find()) {
            val hours = durationMatcher.group(1)?.toIntOrNull()
            val mins = durationMatcher.group(2)?.toIntOrNull()
            durationMin =
                when {
                    hours != null -> hours * 60
                    mins != null -> mins
                    else -> null
                }
            highlights +=
                QuickCaptureHighlight(
                    durationMatcher.start(),
                    durationMatcher.end(),
                    QuickCaptureHighlightKind.Duration,
                )
        }

        val cleaned =
            text
                .replace(TOMORROW_PATTERN.toRegex(), "")
                .replace(TIME_PATTERN.toRegex(), "")
                .replace(DURATION_PATTERN.toRegex(), "")
                .replace(Regex("\\s+"), " ")
                .trim()

        return QuickCaptureParseResult(
            title = cleaned.ifBlank { text.trim() },
            targetDate = targetDate,
            startTime = startTime,
            durationMin = durationMin,
            highlights = highlights,
        )
    }

    private fun toLocalTime(
        hour: Int,
        minute: Int,
        amPm: String?,
    ): LocalTime {
        val h24 =
            when (amPm) {
                "pm" -> if (hour == 12) 12 else hour + 12
                "am" -> if (hour == 12) 0 else hour
                else -> hour
            }
        return LocalTime.of(h24.coerceIn(0, 23), minute.coerceIn(0, 59))
    }
}
