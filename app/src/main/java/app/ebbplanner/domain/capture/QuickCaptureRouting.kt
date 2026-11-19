package app.ebbplanner.domain.capture

import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.ui.today.QuickCaptureSaveTarget
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class QuickCaptureDraft(
    val raw: String,
    val startOverride: LocalTime? = null,
    val durationOverride: Int? = null,
    val startOverridden: Boolean = false,
    val durationOverridden: Boolean = false,
    val dateOverride: LocalDate? = null,
    val dateOverridden: Boolean = false,
)

enum class QuickCaptureDestinationKind {
    Timeline,
    Anytime,
    Inbox,
}

data class QuickCaptureResolved(
    val title: String,
    val date: LocalDate,
    val startTime: LocalTime?,
    val startLocalTime: String?,
    val durationMin: Int,
    val explicitDuration: Boolean,
    val placement: String,
    val kind: QuickCaptureDestinationKind,
) {
    val saveEnabled: Boolean get() = title.isNotBlank()
}

private val clockFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun resolveQuickCapture(
    draft: QuickCaptureDraft,
    today: LocalDate,
    saveTarget: QuickCaptureSaveTarget,
): QuickCaptureResolved {
    val parsed = QuickCaptureParser.parse(draft.raw, today)
    val date =
        if (draft.dateOverridden) {
            draft.dateOverride ?: today
        } else {
            parsed.targetDate ?: today
        }
    val startTime =
        if (draft.startOverridden) {
            draft.startOverride
        } else {
            parsed.startTime
        }
    val explicitDuration =
        if (draft.durationOverridden) {
            draft.durationOverride != null
        } else {
            parsed.durationMin != null
        }
    val durationMin =
        if (draft.durationOverridden) {
            draft.durationOverride ?: TaskEntity.DEFAULT_DURATION_MIN
        } else {
            parsed.durationMin ?: TaskEntity.DEFAULT_DURATION_MIN
        }
    val startLocalTime = startTime?.format(clockFormatter)
    val placement =
        when (saveTarget) {
            QuickCaptureSaveTarget.Inbox ->
                when {
                    startTime != null -> TaskEntity.PLACEMENT_SCHEDULED
                    parsed.targetDate != null -> TaskEntity.PLACEMENT_ANYTIME
                    else -> TaskEntity.PLACEMENT_INBOX
                }
            QuickCaptureSaveTarget.Today, QuickCaptureSaveTarget.Week ->
                if (startTime != null) {
                    TaskEntity.PLACEMENT_SCHEDULED
                } else {
                    TaskEntity.PLACEMENT_ANYTIME
                }
        }
    val kind =
        when (placement) {
            TaskEntity.PLACEMENT_SCHEDULED -> QuickCaptureDestinationKind.Timeline
            TaskEntity.PLACEMENT_INBOX -> QuickCaptureDestinationKind.Inbox
            else -> QuickCaptureDestinationKind.Anytime
        }
    val title = parsed.title.ifBlank { draft.raw.trim() }
    return QuickCaptureResolved(
        title = title,
        date = date,
        startTime = startTime,
        startLocalTime = startLocalTime,
        durationMin = durationMin,
        explicitDuration = explicitDuration,
        placement = placement,
        kind = kind,
    )
}

fun nextFreeSlotMinutes(
    nowMinutes: Int,
    occupied: List<Pair<Int, Int>>,
    durationMin: Int = TaskEntity.DEFAULT_DURATION_MIN,
    windowStart: Int = 0,
    windowEnd: Int = 24 * 60,
): Int? {
    if (durationMin <= 0) return null
    val latestStart = windowEnd - durationMin
    if (latestStart < windowStart) return null
    val roundedNow = ((nowMinutes + 4) / 5) * 5
    var cursor = maxOf(windowStart, roundedNow)
    if (cursor > latestStart) return null
    val sorted = occupied.sortedBy { it.first }
    sorted.forEach { (start, length) ->
        val end = start + length
        if (start >= cursor + durationMin) {
            return cursor
        }
        if (end > cursor) {
            cursor = ((end + 4) / 5) * 5
        }
        if (cursor > latestStart) return null
    }
    return if (cursor <= latestStart) cursor else null
}

fun formatClockHm(time: LocalTime): String = time.format(clockFormatter)
