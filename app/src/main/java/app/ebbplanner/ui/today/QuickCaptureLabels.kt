package app.ebbplanner.ui.today

import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.ebbplanner.R
import app.ebbplanner.domain.capture.QuickCaptureDestinationKind
import app.ebbplanner.domain.capture.QuickCaptureResolved
import app.ebbplanner.ui.time.ClockFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.UK)

@Composable
fun quickCaptureDayLabel(
    date: LocalDate,
    today: LocalDate,
): String =
    when (date) {
        today -> stringResource(R.string.quick_capture_day_today)
        today.plusDays(1) -> stringResource(R.string.quick_capture_day_tomorrow)
        else -> date.format(dayFormatter)
    }

@Composable
fun quickCapturePreviewLabel(
    resolved: QuickCaptureResolved,
    today: LocalDate,
    is24Hour: Boolean,
): String? {
    val day = quickCaptureDayLabel(resolved.date, today)
    val time =
        resolved.startTime?.let {
            ClockFormat.minutesToClockDisplay(it.hour * 60 + it.minute, is24Hour)
        }
    return when {
        time != null && resolved.explicitDuration ->
            stringResource(R.string.quick_capture_preview_time_duration, day, time, resolved.durationMin)
        time != null -> stringResource(R.string.quick_capture_preview_time, day, time)
        resolved.explicitDuration -> stringResource(R.string.quick_capture_preview_duration, resolved.durationMin)
        else -> null
    }
}

@Composable
fun quickCaptureDestinationLabel(
    resolved: QuickCaptureResolved,
    today: LocalDate,
    is24Hour: Boolean,
): String {
    val time =
        resolved.startTime?.let {
            ClockFormat.minutesToClockDisplay(it.hour * 60 + it.minute, is24Hour)
        }.orEmpty()
    return when (resolved.kind) {
        QuickCaptureDestinationKind.Inbox -> stringResource(R.string.quick_capture_dest_inbox)
        QuickCaptureDestinationKind.Anytime -> stringResource(R.string.quick_capture_dest_anytime)
        QuickCaptureDestinationKind.Timeline ->
            when (resolved.date) {
                today -> stringResource(R.string.quick_capture_dest_timeline_today, time)
                today.plusDays(1) -> stringResource(R.string.quick_capture_dest_timeline_tomorrow, time)
                else -> stringResource(R.string.quick_capture_dest_timeline_date, resolved.date.format(dayFormatter), time)
            }
    }
}

@Composable
fun quickCaptureSavedMessage(resolved: QuickCaptureResolved): String =
    quickCaptureSavedMessage(androidx.compose.ui.platform.LocalContext.current.resources, resolved)

fun quickCaptureSavedMessage(
    resources: Resources,
    resolved: QuickCaptureResolved,
): String =
    when (resolved.kind) {
        QuickCaptureDestinationKind.Timeline ->
            resources.getString(R.string.quick_capture_saved_timeline, resolved.startLocalTime.orEmpty())
        QuickCaptureDestinationKind.Anytime -> resources.getString(R.string.quick_capture_saved_anytime)
        QuickCaptureDestinationKind.Inbox -> resources.getString(R.string.quick_capture_saved_inbox)
    }
