package app.ebbplanner.ui.today

import androidx.compose.ui.unit.Dp

enum class TodayDayKind {
    Today,
    Past,
    Future,
    AfterBedtime,
}

enum class TodayVisualMode {
    Normal,
    Loading,
    Empty,
    Drag,
    Overfull,
    Overlap,
    RemindersBanner,
    Dense,
}

data class TodayDragOverlay(
    val ghostBlockId: String,
    val liftedBlockId: String,
    val tooltip: String,
    val liftedTopDp: Dp,
    val scrimAlpha: Float = 0.35f,
)

data class TodayScheduledBlock(
    val id: String,
    val title: String,
    val colorKey: String,
    val iconKey: String,
    val startMinutes: Int,
    val durationMin: Int,
    val done: Boolean = false,
    val isCurrent: Boolean = false,
    val isPast: Boolean = false,
    val short: Boolean = false,
    val energyLevel: Int? = null,
    val checklist: String? = null,
    val hasReminder: Boolean = false,
    val pinned: Boolean = false,
    val minutesLeftLabel: String? = null,
    val metaTimeRange: String? = null,
    val metaDuration: String? = null,
    val isGhost: Boolean = false,
    val isLifted: Boolean = false,
    val selected: Boolean = false,
    val overlapLane: Int = 0,
    val overlapLaneCount: Int = 1,
)

data class TodayCalendarEvent(
    val id: String,
    val title: String,
    val startMinutes: Int,
    val durationMin: Int,
    val calendarLabel: String,
    val colorKey: String = "blue",
    val pinned: Boolean = false,
)

data class TodayTimelineGap(
    val id: String,
    val startMinutes: Int,
    val durationMin: Int,
    val label: String,
)

data class TodayAnytimeChip(
    val id: String,
    val title: String,
)

data class TodayPresentationState(
    val date: java.time.LocalDate = java.time.LocalDate.now(),
    val dateLabel: String = "Mon 16 Nov",
    val dayKind: TodayDayKind = TodayDayKind.Today,
    val timeLeftLabel: String = "12 h 18 min left",
    val dayMeta: String = "today · bedtime 23:00 · 2 done",
    val progressPercent: Int = 30,
    val bedtimeLabel: String = "23:00",
    val doneCount: Int = 2,
    val anytimeChips: List<TodayAnytimeChip> = emptyList(),
    val blocks: List<TodayScheduledBlock> = emptyList(),
    val events: List<TodayCalendarEvent> = emptyList(),
    val gaps: List<TodayTimelineGap> = emptyList(),
    val nowMinutes: Int? = 10 * 60 + 42,
    val showNowLine: Boolean = true,
    val visualMode: TodayVisualMode = TodayVisualMode.Normal,
    val dragOverlay: TodayDragOverlay? = null,
    val bannerMessage: String? = null,
    val overlapHint: String? = null,
    val wakeMinutes: Int = app.ebbplanner.data.preferences.UserPreferencesRepository.DEFAULT_WAKE_MINUTES,
    val bedMinutes: Int = app.ebbplanner.data.preferences.UserPreferencesRepository.DEFAULT_BED_MINUTES,
    val tomorrowOccupied: List<Pair<Int, Int>> = emptyList(),
    val timelineStartHour: Int = 9,
    val timelineEndHour: Int = 17,
    val compactHourRail: Boolean = false,
    val titleOnlyBlocks: Boolean = false,
    val showFab: Boolean = true,
    val quickCapture: QuickCapturePresentationState? = null,
)
