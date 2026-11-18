package app.ebbplanner.ui.today

/** Minimum drawn heights on the timeline map to minutes at 72dp/h. */
internal const val TIMELINE_MIN_SHORT_DRAW_MINUTES = 20
internal const val TIMELINE_MIN_STANDARD_DRAW_MINUTES = 34

internal fun effectiveTimelineEndMinutes(block: TodayScheduledBlock): Int {
    val floor =
        if (block.short) {
            TIMELINE_MIN_SHORT_DRAW_MINUTES
        } else {
            TIMELINE_MIN_STANDARD_DRAW_MINUTES
        }
    return block.startMinutes + maxOf(block.durationMin, floor)
}