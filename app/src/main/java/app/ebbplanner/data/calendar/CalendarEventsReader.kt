package app.ebbplanner.data.calendar

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

data class CalendarEventSnapshot(
    val id: Long,
    val title: String,
    val startMillis: Long,
    val endMillis: Long,
    val calendarName: String,
)

@Singleton
class CalendarEventsReader
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        fun eventsForDay(
            date: LocalDate,
            zoneId: ZoneId = ZoneId.systemDefault(),
        ): List<CalendarEventSnapshot> {
            if (
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                return emptyList()
            }
            val start = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val end = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
            val resolver: ContentResolver = context.contentResolver
            val projection =
                arrayOf(
                    CalendarContract.Events._ID,
                    CalendarContract.Events.TITLE,
                    CalendarContract.Events.DTSTART,
                    CalendarContract.Events.DTEND,
                    CalendarContract.Events.CALENDAR_DISPLAY_NAME,
                )
            val selection =
                "(${CalendarContract.Events.DTSTART} < ?) AND (${CalendarContract.Events.DTEND} > ?)"
            val args = arrayOf(end.toString(), start.toString())
            val result = mutableListOf<CalendarEventSnapshot>()
            resolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                args,
                "${CalendarContract.Events.DTSTART} ASC",
            )?.use { cursor ->
                val idIdx = cursor.getColumnIndexOrThrow(CalendarContract.Events._ID)
                val titleIdx = cursor.getColumnIndexOrThrow(CalendarContract.Events.TITLE)
                val startIdx = cursor.getColumnIndexOrThrow(CalendarContract.Events.DTSTART)
                val endIdx = cursor.getColumnIndexOrThrow(CalendarContract.Events.DTEND)
                val calIdx = cursor.getColumnIndexOrThrow(CalendarContract.Events.CALENDAR_DISPLAY_NAME)
                while (cursor.moveToNext()) {
                    result +=
                        CalendarEventSnapshot(
                            id = cursor.getLong(idIdx),
                            title = cursor.getString(titleIdx).orEmpty(),
                            startMillis = cursor.getLong(startIdx),
                            endMillis = cursor.getLong(endIdx),
                            calendarName = cursor.getString(calIdx).orEmpty(),
                        )
                }
            }
            return result
        }
    }
