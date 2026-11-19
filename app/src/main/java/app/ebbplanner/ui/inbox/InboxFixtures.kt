@file:Suppress("MagicNumber", "MaxLineLength", "TooManyFunctions", "NewLineAtEndOfFile")

package app.ebbplanner.ui.inbox

private fun baseTasks(): List<InboxTaskRow> =
    listOf(
        InboxTaskRow("1", "Buy stamps", "lemon", "local_post_office", "10 min", 1),
        InboxTaskRow("2", "Book dentist check-up for March", "rose", "medical_services", "5 min", 1, hasReminder = true),
        InboxTaskRow("3", "Fix the squeaky door hinge", "slate", "handyman", "15 min", 2),
        InboxTaskRow(
            "4",
            "Research standing desks and compare the two I liked, then decide by Friday",
            "blue",
            "chair",
            "45 min",
            2,
            checklist = "1/4",
        ),
        InboxTaskRow("5", "Renew passport", "amber", "badge", "1 h", 3, checklist = "0/5"),
        InboxTaskRow("6", "Send birthday card to Lina", "rose", "cake", "10 min", 1),
        InboxTaskRow("7", "Clean out the fridge", "green", "kitchen", "30 min", 2),
    )

fun inboxPopulated(): InboxPresentationState = InboxPresentationState(tasks = baseTasks())

fun inboxEmpty(): InboxPresentationState =
    InboxPresentationState(
        subtitle = "0 thoughts",
        tasks = emptyList(),
        visualMode = InboxVisualMode.Empty,
        emptyMessage = "Nothing in your Inbox. Capture a thought anytime.",
    )

fun inboxFilteredEmpty(): InboxPresentationState =
    InboxPresentationState(
        subtitle = "12 thoughts",
        selectedFilter = 1,
        tasks = emptyList(),
        visualMode = InboxVisualMode.FilteredEmpty,
        emptyMessage = "No quick tasks right now. Try another filter.",
    )

fun inboxMany(): InboxPresentationState =
    InboxPresentationState(
        subtitle = "24 thoughts",
        tasks = baseTasks() + baseTasks().mapIndexed { i, row -> row.copy(id = "${row.id}_$i") },
        visualMode = InboxVisualMode.Many,
    )

fun inboxLoading(): InboxPresentationState =
    InboxPresentationState(
        tasks = emptyList(),
        visualMode = InboxVisualMode.Loading,
    )

fun inboxMultiselect(): InboxPresentationState =
    inboxPopulated().copy(
        title = "2 selected",
        visualMode = InboxVisualMode.Multiselect,
        showFab = false,
        tasks = baseTasks().map { it.copy(selected = it.id == "1" || it.id == "4") },
    )

fun inboxMultiselectOne(): InboxPresentationState =
    inboxPopulated().copy(
        title = "1 selected",
        visualMode = InboxVisualMode.Multiselect,
        showFab = false,
        tasks = baseTasks().map { it.copy(selected = it.id == "1") },
    )

fun inboxSwipeLeft(): InboxPresentationState =
    inboxPopulated().copy(
        visualMode = InboxVisualMode.SwipeLeft,
        swipeReveal =
            InboxSwipeReveal(
                taskId = "1",
                label = "Later…",
                icon = "schedule",
                side = InboxSwipeSide.Left,
            ),
    )

fun inboxSwipeRight(): InboxPresentationState =
    inboxPopulated().copy(
        visualMode = InboxVisualMode.SwipeRight,
        swipeReveal =
            InboxSwipeReveal(
                taskId = "1",
                label = "Schedule",
                icon = "event_available",
                side = InboxSwipeSide.Right,
            ),
    )

fun inboxDragToToday(): InboxPresentationState =
    inboxPopulated().copy(
        visualMode = InboxVisualMode.DragToToday,
        dragToTodayHint = "Drop on Today to schedule",
    )

fun inboxTidyUp(): InboxPresentationState =
    inboxPopulated().copy(
        visualMode = InboxVisualMode.TidyUp,
        dragToTodayHint = "Tidy up: 3 duplicates found",
    )
