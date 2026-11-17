@file:Suppress("MagicNumber", "MaxLineLength")

package app.ebbplanner.ui.inbox

fun inboxPopulated(): InboxPresentationState =
    InboxPresentationState(
        tasks =
            listOf(
                InboxTaskRow("1", "Buy stamps", "lemon", "local_post_office", "10 min", 1),
                InboxTaskRow("2", "Book dentist check-up for March", "rose", "medical_services", "5 min", 1, true),
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
            ),
    )

fun inboxEmpty(): InboxPresentationState =
    InboxPresentationState(
        subtitle = "0 thoughts",
        tasks = emptyList(),
        visualMode = InboxVisualMode.Empty,
    )

fun inboxDragToToday(): InboxPresentationState = inboxPopulated().copy(visualMode = InboxVisualMode.DragToToday)
