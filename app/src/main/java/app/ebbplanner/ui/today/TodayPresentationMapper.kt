@file:Suppress("ReturnCount")

package app.ebbplanner.ui.today

import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.ui.time.ClockFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object TodayPresentationMapper {
    private val dateFormatter = DateTimeFormatter.ofPattern("EEE d MMM")

    fun from(
        state: TodayUiState,
        nowMinutes: Int = defaultNowMinutes(),
    ): TodayPresentationState {
        if (state.isLoading) {
            return TodayPresentationState(
                dateLabel = state.dateLabel.ifBlank { state.date.format(dateFormatter) },
                visualMode = TodayVisualMode.Loading,
                showNowLine = false,
            )
        }
        val scheduled = state.scheduledTasks
        if (scheduled.isEmpty() && state.anytimeTasks.isEmpty()) {
            return todayEmpty().copy(
                dateLabel = state.dateLabel.ifBlank { state.date.format(dateFormatter) },
                dayMeta = "today · bedtime ${formatBed(state.bedMinutes)} · 0 done",
                timeLeftLabel = formatTimeLeft(state.bedMinutes, nowMinutes),
            )
        }
        val blocks = assignOverlapLanes(scheduled.mapNotNull { task -> task.toBlock(nowMinutes) })
        val minutesLeft = (state.bedMinutes - nowMinutes).coerceAtLeast(0)
        val plannedRemaining = scheduled.filter { it.status != TaskEntity.STATUS_DONE }.sumOf { it.durationMin }
        val overfull = isToday(state.date) && plannedRemaining > minutesLeft
        val overlap = detectOverlap(blocks)
        val events = state.calendarEvents.map { it.toPresentation() }
        val timeline = computeTimelineWindow(blocks, events)
        return TodayPresentationState(
            dateLabel = state.dateLabel.ifBlank { state.date.format(dateFormatter) },
            timeLeftLabel = formatMinutesLong(minutesLeft),
            dayMeta =
                "today · bedtime ${formatBed(state.bedMinutes)} · " +
                    "${scheduled.count { it.status == TaskEntity.STATUS_DONE }} done",
            progressPercent =
                progressPercent(
                    state.scheduledTasks.filter { it.status == TaskEntity.STATUS_DONE }.sumOf { it.durationMin },
                    state.wakeMinutes,
                    state.bedMinutes,
                ),
            anytimeChips = state.anytimeTasks.map { TodayAnytimeChip(it.id, it.title) },
            blocks = blocks,
            events = events,
            timelineStartHour = timeline.first,
            timelineEndHour = timeline.second,
            nowMinutes = if (isToday(state.date)) nowMinutes else null,
            showNowLine = isToday(state.date),
            bannerMessage = if (overfull) "Fuller than the time left · Make it fit" else null,
            overlapHint = if (overlap) "These overlap" else null,
            visualMode =
                when {
                    overlap -> TodayVisualMode.Overlap
                    overfull -> TodayVisualMode.Overfull
                    else -> TodayVisualMode.Normal
                },
        )
    }

    private const val TIMELINE_MIN_START_HOUR = 9
    private const val TIMELINE_MIN_END_HOUR = 17

    private fun computeTimelineWindow(
        blocks: List<TodayScheduledBlock>,
        events: List<TodayCalendarEvent>,
    ): Pair<Int, Int> {
        val starts = blocks.map { it.startMinutes } + events.map { it.startMinutes }
        val ends =
            blocks.map { it.startMinutes + it.durationMin } +
                events.map { it.startMinutes + it.durationMin }
        val earliest = starts.minOrNull()
        val latest = ends.maxOrNull()
        val startHour =
            earliest?.let { minOf(TIMELINE_MIN_START_HOUR, it / 60) } ?: TIMELINE_MIN_START_HOUR
        val endHour =
            latest?.let { maxOf(TIMELINE_MIN_END_HOUR, (it + 59) / 60) } ?: TIMELINE_MIN_END_HOUR
        return startHour.coerceAtLeast(0) to endHour.coerceAtMost(24).coerceAtLeast(startHour + 1)
    }

    private fun assignOverlapLanes(blocks: List<TodayScheduledBlock>): List<TodayScheduledBlock> {
        if (blocks.isEmpty()) return blocks
        val sorted = blocks.sortedBy { it.startMinutes }
        val laneEnds = mutableListOf<Int>()
        val laneById = mutableMapOf<String, Int>()
        sorted.forEach { block ->
            val end = effectiveTimelineEndMinutes(block)
            var lane = laneEnds.indexOfFirst { it <= block.startMinutes }
            if (lane < 0) {
                lane = laneEnds.size
                laneEnds.add(end)
            } else {
                laneEnds[lane] = end
            }
            laneById[block.id] = lane
        }
        val maxLanes = laneEnds.size.coerceAtLeast(1)
        return blocks.map { block ->
            val overlaps =
                blocks.any { other ->
                    other.id != block.id &&
                        block.startMinutes < effectiveTimelineEndMinutes(other) &&
                        other.startMinutes < effectiveTimelineEndMinutes(block)
                }
            block.copy(
                overlapLane = laneById[block.id] ?: 0,
                overlapLaneCount = if (overlaps) maxLanes.coerceAtLeast(2) else 1,
            )
        }
    }

    private fun detectOverlap(blocks: List<TodayScheduledBlock>): Boolean {
        for (i in blocks.indices) {
            val a = blocks[i]
            val aEnd = a.startMinutes + a.durationMin
            for (j in i + 1 until blocks.size) {
                val b = blocks[j]
                val bEnd = b.startMinutes + b.durationMin
                if (a.startMinutes < bEnd && b.startMinutes < aEnd) return true
            }
        }
        return false
    }

    private fun TaskEntity.toBlock(nowMinutes: Int): TodayScheduledBlock? {
        val start = startLocalTime?.let { ClockFormat.parseMinutes(it) } ?: return null
        val isDone = status == TaskEntity.STATUS_DONE
        val end = start + durationMin
        return TodayScheduledBlock(
            id = id,
            title = title,
            colorKey = colorKey,
            iconKey = iconKey ?: "task",
            startMinutes = start,
            durationMin = durationMin,
            done = isDone,
            isPast = end <= nowMinutes,
            isCurrent = !isDone && start <= nowMinutes && end > nowMinutes,
            short = durationMin < 20,
            pinned = pinned,
            hasReminder = reminderEnabled,
            metaTimeRange = "${ClockFormat.minutesToClock(start)}–${ClockFormat.minutesToClock(end)}",
            metaDuration = "$durationMin min",
            minutesLeftLabel =
                if (!isDone && start <= nowMinutes && end > nowMinutes) {
                    "${end - nowMinutes} min left"
                } else {
                    null
                },
        )
    }

    private fun isToday(date: LocalDate): Boolean = date == LocalDate.now()

    private fun defaultNowMinutes(): Int {
        val now = java.time.LocalTime.now()
        return now.hour * 60 + now.minute
    }

    private fun formatBed(minutes: Int): String = ClockFormat.minutesToClock(minutes)

    private fun formatTimeLeft(
        bed: Int,
        now: Int,
    ): String = formatMinutesLong((bed - now).coerceAtLeast(0))

    private fun formatMinutesLong(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return when {
            h > 0 && m > 0 -> "$h h $m min left"
            h > 0 -> "$h h left"
            else -> "$m min left"
        }
    }

    private fun progressPercent(
        planned: Int,
        wake: Int,
        bed: Int,
    ): Int {
        val window = (bed - wake).coerceAtLeast(1)
        return ((planned.toFloat() / window) * 100).toInt().coerceIn(0, 100)
    }
}
