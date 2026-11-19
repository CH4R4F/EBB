@file:Suppress("ReturnCount", "NewLineAtEndOfFile")

package app.ebbplanner.ui.time

object ClockFormat {
    fun minutesToClock(minutes: Int): String {
        val normalized = ((minutes % (24 * 60)) + (24 * 60)) % (24 * 60)
        val h = normalized / 60
        val m = normalized % 60
        return "%d:%02d".format(h, m)
    }

    fun localTimeToClock(value: String): String {
        val parts = value.split(":")
        if (parts.size != 2) return value
        val h = parts[0].toIntOrNull() ?: return value
        val m = parts[1].toIntOrNull() ?: 0
        return minutesToClock(h * 60 + m)
    }

    fun parseMinutes(value: String): Int? {
        val parts = value.split(":")
        if (parts.size != 2) return null
        val h = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: 0
        return h * 60 + m
    }

    fun minutesToLocalTime(minutes: Int): String {
        val normalized = ((minutes % (24 * 60)) + (24 * 60)) % (24 * 60)
        return "%02d:%02d".format(normalized / 60, normalized % 60)
    }

    fun minutesToClockDisplay(
        minutes: Int,
        is24Hour: Boolean,
    ): String {
        val normalized = ((minutes % (24 * 60)) + (24 * 60)) % (24 * 60)
        val h = normalized / 60
        val m = normalized % 60
        return if (is24Hour) {
            "%02d:%02d".format(h, m)
        } else {
            "%d:%02d %s".format(hour12(h), m, meridian(h))
        }
    }

    fun minutesToClockCompact(
        minutes: Int,
        is24Hour: Boolean,
    ): String {
        if (is24Hour) return minutesToClock(minutes)
        val normalized = ((minutes % (24 * 60)) + (24 * 60)) % (24 * 60)
        val h = normalized / 60
        val m = normalized % 60
        return "%d:%02d".format(hour12(h), m)
    }

    fun localTimeToClockDisplay(
        value: String,
        is24Hour: Boolean,
    ): String {
        val minutes = parseMinutes(value) ?: return value
        return minutesToClockDisplay(minutes, is24Hour)
    }

    fun rangeDisplay(
        startMinutes: Int,
        endMinutes: Int,
        is24Hour: Boolean,
    ): String {
        val startLabel = minutesToClockDisplay(startMinutes, is24Hour)
        val endLabel = minutesToClockDisplay(endMinutes, is24Hour)
        if (is24Hour) return "$startLabel–$endLabel"
        val startH = normalizedHour(startMinutes)
        val endH = normalizedHour(endMinutes)
        if (meridian(startH) == meridian(endH)) {
            return "${minutesToClockCompact(startMinutes, is24Hour = false)}–$endLabel"
        }
        return "$startLabel–$endLabel"
    }

    private fun normalizedHour(minutes: Int): Int {
        val normalized = ((minutes % (24 * 60)) + (24 * 60)) % (24 * 60)
        return normalized / 60
    }

    private fun hour12(hour24: Int): Int =
        when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }

    private fun meridian(hour24: Int): String = if (hour24 < 12) "AM" else "PM"

    fun isSystem24Hour(context: android.content.Context): Boolean =
        android.text.format.DateFormat.is24HourFormat(context)
}
