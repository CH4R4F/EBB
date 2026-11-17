@file:Suppress("LongMethod", "MagicNumber", "ReturnCount", "MaxLineLength", "LoopWithTooManyJumpStatements")

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
    private val COLON_TIME = Pattern.compile("""\b([01]?\d|2[0-3]):([0-5]\d)\b""")
    private val AMPM_TIME = Pattern.compile("""(?i)\b(\d{1,2})(?::(\d{2}))?\s*(am|pm)\b""")
    private val AT_TIME =
        Pattern.compile(
            """(?i)\bat\s+(\d{1,2})(?::(\d{2}))?\s*(am|pm)\b|\bat\s+([01]?\d|2[0-3]):([0-5]\d)\b""",
        )
    private val DURATION_HOURS = Pattern.compile("""(?i)\b(\d+)\s*(?:h|hr|hrs|hour|hours)\b""")
    private val DURATION_MINUTES = Pattern.compile("""(?i)\b(\d+)\s*(?:m|min|mins|minute|minutes)\b""")
    private val TOMORROW = Pattern.compile("""(?i)\btomorrow\b""")

    fun parse(
        text: String,
        today: LocalDate = LocalDate.now(),
    ): QuickCaptureParseResult {
        val highlights = mutableListOf<QuickCaptureHighlight>()
        var targetDate: LocalDate? = null
        var startTime: LocalTime? = null
        var durationMin: Int? = null
        val removeRanges = mutableListOf<IntRange>()

        val tomorrowMatcher = TOMORROW.matcher(text)
        if (tomorrowMatcher.find()) {
            targetDate = today.plusDays(1)
            val range = tomorrowMatcher.start() until tomorrowMatcher.end()
            removeRanges += range
            highlights += QuickCaptureHighlight(range.first, range.last + 1, QuickCaptureHighlightKind.Tomorrow)
        }

        val durationHours = DURATION_HOURS.matcher(text)
        if (durationHours.find()) {
            durationMin = durationHours.group(1)?.toIntOrNull()?.times(60)
            val range = durationHours.start() until durationHours.end()
            removeRanges += range
            highlights += QuickCaptureHighlight(range.first, range.last + 1, QuickCaptureHighlightKind.Duration)
        } else {
            val durationMinutes = DURATION_MINUTES.matcher(text)
            while (durationMinutes.find()) {
                val start = durationMinutes.start()
                val precededByColon = start > 0 && text[start - 1] == ':'
                if (precededByColon) continue
                durationMin = durationMinutes.group(1)?.toIntOrNull()
                val range = durationMinutes.start() until durationMinutes.end()
                removeRanges += range
                highlights += QuickCaptureHighlight(range.first, range.last + 1, QuickCaptureHighlightKind.Duration)
                break
            }
        }

        val timeRange =
            findFirstTimeMatch(text)?.also { match ->
                startTime = match.time
                removeRanges += match.range
                highlights += QuickCaptureHighlight(match.range.first, match.range.last + 1, QuickCaptureHighlightKind.Time)
            }

        val cleaned = buildTitle(text, removeRanges)
        val title =
            when {
                cleaned.isNotBlank() -> cleaned
                text.trim().equals("tomorrow", ignoreCase = true) -> "tomorrow"
                timeRange != null && durationMin != null -> text.trim()
                else -> text.trim()
            }

        return QuickCaptureParseResult(
            title = title,
            targetDate = targetDate,
            startTime = startTime,
            durationMin = durationMin,
            highlights = highlights,
        )
    }

    private data class TimeMatch(
        val time: LocalTime,
        val range: IntRange,
    )

    private fun findFirstTimeMatch(text: String): TimeMatch? {
        val at = AT_TIME.matcher(text)
        if (at.find()) {
            val hour = at.group(1)?.toIntOrNull() ?: at.group(4)?.toIntOrNull()
            val minute = at.group(2)?.toIntOrNull() ?: at.group(5)?.toIntOrNull() ?: 0
            val suffix = at.group(3)?.lowercase(Locale.US)
            if (hour != null) {
                return TimeMatch(toLocalTime(hour, minute, suffix), at.start() until at.end())
            }
        }
        val colon = COLON_TIME.matcher(text)
        if (colon.find()) {
            val hour = colon.group(1)?.toIntOrNull() ?: return null
            val minute = colon.group(2)?.toIntOrNull() ?: 0
            return TimeMatch(LocalTime.of(hour, minute), colon.start() until colon.end())
        }
        val amPm = AMPM_TIME.matcher(text)
        if (amPm.find()) {
            val hour = amPm.group(1)?.toIntOrNull() ?: return null
            val minute = amPm.group(2)?.toIntOrNull() ?: 0
            val suffix = amPm.group(3)?.lowercase(Locale.US)
            return TimeMatch(toLocalTime(hour, minute, suffix), amPm.start() until amPm.end())
        }
        return null
    }

    private fun buildTitle(
        text: String,
        ranges: List<IntRange>,
    ): String {
        if (ranges.isEmpty()) return text.trim()
        val sorted = ranges.sortedByDescending { it.first }
        var result = text
        sorted.forEach { range ->
            result = result.removeRange(range)
        }
        return result.replace(Regex("\\s+"), " ").trim()
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
