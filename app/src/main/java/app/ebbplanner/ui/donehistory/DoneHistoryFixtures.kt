@file:Suppress("NewLineAtEndOfFile")

package app.ebbplanner.ui.donehistory

fun doneHistoryPopulated(): DoneHistoryPresentationState =
    DoneHistoryPresentationState(
        sections =
            listOf(
                DoneSection(
                    "Today · 5 done",
                    listOf(
                        DoneItem("Write project brief", "11:12"),
                        DoneItem("Emails", "09:52"),
                        DoneItem("Work start-up", "09:04"),
                        DoneItem("Take meds", "08:02"),
                        DoneItem("Make bed", "07:40"),
                    ),
                ),
                DoneSection(
                    "Sun 15 Nov · 7 done",
                    listOf(
                        DoneItem("Groceries", "16:10"),
                        DoneItem("Laundry", "11:58"),
                        DoneItem("Weekly reset", "10:55"),
                    ),
                ),
                DoneSection(
                    "Let go · kept for 30 days",
                    listOf(
                        DoneItem("Clean garage", "", restore = true, subtitle = "Let go Fri 13 Nov", icon = "spa"),
                    ),
                ),
            ),
    )

fun doneHistoryEmpty(): DoneHistoryPresentationState =
    DoneHistoryPresentationState(
        sections = emptyList(),
        mode = DoneHistoryMode.Empty,
    )
