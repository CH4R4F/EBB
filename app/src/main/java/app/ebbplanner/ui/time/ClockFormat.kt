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
}