@file:Suppress("ReturnCount")

package app.ebbplanner.ui.today

import app.ebbplanner.data.local.entity.TaskEntity
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
        val blocks = scheduled.mapNotNull { task -> task.toBlock(nowMinutes) }
        val minutesLeft = (state.bedMinutes - nowMinutes).coerceAtLeast(0)
        return TodayPresentationState(
            dateLabel = state.dateLabel.ifBlank { state.date.format(dateFormatter) },
            timeLeftLabel = formatMinutesLong(minutesLeft),
            dayMeta =
                "today · bedtime ${formatBed(state.bedMinutes)} · " +
                    "${scheduled.count { it.status == TaskEntity.STATUS_DONE }} done",
            progressPercent = progressPercent(state.minutesPlanned, state.wakeMinutes, state.bedMinutes),
            anytimeChips = state.anytimeTasks.map { it.title },
            blocks = blocks,
            events = state.calendarEvents.map { it.toPresentation() },
            nowMinutes = if (isToday(state.date)) nowMinutes else null,
            showNowLine = isToday(state.date),
        )
    }

    private fun TaskEntity.toBlock(nowMinutes: Int): TodayScheduledBlock? {
        val start = startLocalTime?.let { parseMinutes(it) } ?: return null
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
            metaTimeRange = "${formatClock(start)}–${formatClock(end)}",
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

    private fun parseMinutes(value: String): Int? {
        val parts = value.split(":")
        if (parts.size != 2) return null
        val h = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: 0
        return h * 60 + m
    }

    private fun formatClock(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return "%d:%02d".format(h, m)
    }

    private fun formatBed(minutes: Int): String = formatClock(minutes)

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
