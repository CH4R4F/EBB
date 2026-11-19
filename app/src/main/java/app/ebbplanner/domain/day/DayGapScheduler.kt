package app.ebbplanner.domain.day

import app.ebbplanner.ui.time.ClockFormat

data class MinuteInterval(
    val start: Int,
    val end: Int,
)

object DayGapScheduler {
    fun roundUpToFive(minutes: Int): Int = ((minutes + 4) / 5) * 5

    fun mergeBusy(intervals: List<MinuteInterval>): List<MinuteInterval> {
        if (intervals.isEmpty()) return emptyList()
        val sorted = intervals.sortedBy { it.start }
        val out = mutableListOf<MinuteInterval>()
        var cur = sorted.first()
        for (i in 1 until sorted.size) {
            val next = sorted[i]
            if (next.start <= cur.end) {
                cur = cur.copy(end = maxOf(cur.end, next.end))
            } else {
                out += cur
                cur = next
            }
        }
        out += cur
        return out
    }

    fun placeTasksInGaps(
        taskDurations: List<Pair<String, Int>>,
        busy: List<MinuteInterval>,
        cursorStart: Int,
        bedMinutes: Int,
        gapAfter: Int = 5,
    ): Map<String, Int> {
        val placements = mutableMapOf<String, Int>()
        val mergedMutable = mergeBusy(busy).toMutableList()
        var cursor = cursorStart
        for ((id, duration) in taskDurations) {
            val start = findSlot(cursor, duration, mergedMutable, bedMinutes) ?: break
            placements[id] = start
            mergedMutable += MinuteInterval(start, start + duration)
            val sorted = mergeBusy(mergedMutable)
            mergedMutable.clear()
            mergedMutable.addAll(sorted)
            cursor = start + duration + gapAfter
        }
        return placements
    }

    fun findSlotPublic(
        cursor: Int,
        duration: Int,
        busy: List<MinuteInterval>,
        bed: Int,
    ): Int? = findSlot(cursor, duration, busy, bed)

    private fun findSlot(
        cursor: Int,
        duration: Int,
        busy: List<MinuteInterval>,
        bed: Int,
    ): Int? {
        var pos = cursor
        while (pos + duration <= bed) {
            val end = pos + duration
            val conflict = busy.any { pos < it.end && end > it.start }
            if (!conflict) return pos
            val bump = busy.filter { pos < it.end && end > it.start }.maxOfOrNull { it.end } ?: (pos + 5)
            pos = bump
        }
        return null
    }

    fun busyFromScheduled(starts: List<Triple<String, Int, Int>>): List<MinuteInterval> =
        starts.map { (_, start, duration) -> MinuteInterval(start, start + duration) }

    fun formatRange(
        start: Int,
        duration: Int,
        is24Hour: Boolean = true,
    ): String {
        val end = start + duration
        return ClockFormat.rangeDisplay(start, end, is24Hour)
    }
}
