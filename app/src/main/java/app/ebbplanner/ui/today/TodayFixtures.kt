@file:Suppress("MagicNumber", "LongMethod", "TooManyFunctions", "MaxLineLength")

package app.ebbplanner.ui.today

import androidx.compose.ui.unit.dp

private fun min(
    h: Int,
    m: Int = 0,
): Int = h * 60 + m

private fun populatedBlocks(): List<TodayScheduledBlock> =
    listOf(
        TodayScheduledBlock(
            id = "startup",
            title = "Work start-up",
            colorKey = "rose",
            iconKey = "wb_sunny",
            startMinutes = min(8, 45),
            durationMin = 20,
            done = true,
            isPast = true,
            short = true,
            metaDuration = "5/5",
        ),
        TodayScheduledBlock(
            id = "emails",
            title = "Emails",
            colorKey = "sky",
            iconKey = "mail",
            startMinutes = min(9, 10),
            durationMin = 45,
            done = true,
            isPast = true,
            metaTimeRange = "09:10–09:55",
            metaDuration = "45 min",
            energyLevel = 1,
        ),
        TodayScheduledBlock(
            id = "brief",
            title = "Write project brief",
            colorKey = "amber",
            iconKey = "edit_note",
            startMinutes = min(10),
            durationMin = 75,
            isCurrent = true,
            metaTimeRange = "10:00–11:15",
            metaDuration = "1 h 15 min",
            energyLevel = 3,
            checklist = "2/5",
            hasReminder = true,
            minutesLeftLabel = "33 min left",
        ),
        TodayScheduledBlock(
            id = "bank",
            title = "Call bank",
            colorKey = "green",
            iconKey = "call",
            startMinutes = min(11, 20),
            durationMin = 25,
            short = true,
            energyLevel = 2,
            hasReminder = true,
            metaDuration = "11:20 · 25 min",
        ),
        TodayScheduledBlock(
            id = "stamps",
            title = "Buy stamps",
            colorKey = "lemon",
            iconKey = "local_post_office",
            startMinutes = min(13, 30),
            durationMin = 15,
            short = true,
            metaDuration = "13:30 · 15 min",
        ),
        TodayScheduledBlock(
            id = "poster",
            title = "Design poster",
            colorKey = "blue",
            iconKey = "brush",
            startMinutes = min(14),
            durationMin = 60,
            metaTimeRange = "14:00–15:00",
            metaDuration = "1 h",
            energyLevel = 3,
            checklist = "0/3",
        ),
        TodayScheduledBlock(
            id = "dentist",
            title = "Dentist",
            colorKey = "vermillion",
            iconKey = "medical_services",
            startMinutes = min(15, 30),
            durationMin = 45,
            metaTimeRange = "15:30–16:15",
            metaDuration = "45 min",
            hasReminder = true,
            pinned = true,
        ),
    )

private fun populatedEvents(): List<TodayCalendarEvent> =
    listOf(
        TodayCalendarEvent(
            id = "lunch",
            title = "Lunch with Ines",
            startMinutes = min(12),
            durationMin = 60,
            calendarLabel = "Personal",
            pinned = true,
        ),
    )

private fun populatedGaps(): List<TodayTimelineGap> =
    listOf(
        TodayTimelineGap(
            id = "gap1",
            startMinutes = min(13, 5),
            durationMin = 25,
            label = "Free · 25 min",
        ),
    )

private fun basePopulated(): TodayPresentationState =
    TodayPresentationState(
        blocks = populatedBlocks(),
        events = populatedEvents(),
        gaps = populatedGaps(),
    )

fun todayPopulatedDark(): TodayPresentationState = basePopulated()

fun todayPopulatedLight(): TodayPresentationState = basePopulated()

fun todayEmpty(): TodayPresentationState =
    TodayPresentationState(
        blocks = emptyList(),
        events = emptyList(),
        gaps = emptyList(),
        anytimeChips = emptyList(),
        progressPercent = 0,
        dayMeta = "today · bedtime 23:00 · 0 done",
        timeLeftLabel = "13 h left",
        visualMode = TodayVisualMode.Empty,
        showNowLine = false,
        nowMinutes = null,
    )

fun todayLoading(): TodayPresentationState =
    TodayPresentationState(
        blocks = emptyList(),
        events = emptyList(),
        gaps = emptyList(),
        visualMode = TodayVisualMode.Loading,
        showNowLine = false,
        nowMinutes = null,
    )

fun todayDrag(): TodayPresentationState {
    val blocks =
        populatedBlocks().map { block ->
            when (block.id) {
                "poster" -> block.copy(isGhost = true)
                else -> block
            }
        } +
            listOf(
                TodayScheduledBlock(
                    id = "poster-lift",
                    title = "Design poster",
                    colorKey = "blue",
                    iconKey = "brush",
                    startMinutes = min(12, 55),
                    durationMin = 60,
                    isLifted = true,
                    metaTimeRange = "12:55–13:55",
                    metaDuration = "1 h",
                    checklist = "0/3",
                ),
            )
    return basePopulated().copy(
        blocks = blocks,
        visualMode = TodayVisualMode.Drag,
        dragOverlay =
            TodayDragOverlay(
                ghostBlockId = "poster",
                liftedBlockId = "poster-lift",
                tooltip = "13:05–14:05 · 1 h",
                liftedTopDp = 283.dp,
            ),
    )
}

fun todayRemindersBanner(): TodayPresentationState =
    basePopulated().copy(
        visualMode = TodayVisualMode.RemindersBanner,
        bannerMessage = "Reminders may be late · Fix",
    )

fun todayOverfull(): TodayPresentationState =
    basePopulated().copy(
        visualMode = TodayVisualMode.Overfull,
        bannerMessage = "Fuller than the time left · Make it fit",
    )

fun todayOverlap(): TodayPresentationState =
    basePopulated().copy(
        visualMode = TodayVisualMode.Overlap,
        overlapHint = "2 blocks overlap · Resolve",
    )

fun todayDense(): TodayPresentationState =
    basePopulated().copy(
        visualMode = TodayVisualMode.Dense,
        blocks = populatedBlocks() + populatedBlocks().mapIndexed { i, b -> b.copy(id = "${b.id}_$i") },
    )

fun todayPastDay(): TodayPresentationState =
    basePopulated().copy(
        dayKind = TodayDayKind.Past,
        dateLabel = "Sun 15 Nov",
        dayMeta = "past day · 4 done",
        showNowLine = false,
        nowMinutes = null,
        blocks = populatedBlocks().map { it.copy(isPast = true, isCurrent = false, done = true) },
    )

fun todayFutureDay(): TodayPresentationState =
    basePopulated().copy(
        dayKind = TodayDayKind.Future,
        dateLabel = "Tue 17 Nov",
        dayMeta = "tomorrow · bedtime 23:00",
        showNowLine = false,
        nowMinutes = null,
        blocks = populatedBlocks().map { it.copy(isPast = false, isCurrent = false) },
    )

fun todayAfterBedtime(): TodayPresentationState =
    basePopulated().copy(
        dayKind = TodayDayKind.AfterBedtime,
        bannerMessage = "Day's done. Close it?",
        showNowLine = false,
        nowMinutes = null,
    )

fun todayCompactTypography(): TodayPresentationState =
    basePopulated().copy(
        compactHourRail = true,
        titleOnlyBlocks = true,
    )

fun todayWithQuickCapture(capture: QuickCapturePresentationState): TodayPresentationState = basePopulated().copy(quickCapture = capture)
