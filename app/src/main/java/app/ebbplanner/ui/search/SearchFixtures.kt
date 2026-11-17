@file:Suppress("MaxLineLength", "NewLineAtEndOfFile")

package app.ebbplanner.ui.search

fun searchResults(): SearchPresentationState =
    SearchPresentationState(
        query = "ca",
        sections =
            listOf(
                SearchSection(
                    "Today",
                    listOf(SearchHit("Call bank", 0, 2, "11:20 · 30 min", "call")),
                ),
                SearchSection(
                    "Upcoming",
                    listOf(
                        SearchHit("Call gran", 0, 2, "Sat 21 Nov · Anytime", "call"),
                        SearchHit("Car service", 0, 2, "Thu 19 Nov · 09:00", "directions_car"),
                    ),
                ),
                SearchSection(
                    "Inbox",
                    listOf(SearchHit("Send birthday card to Lina", 15, 2, "Inbox · 10 min", "cake")),
                ),
                SearchSection(
                    "Routines",
                    listOf(SearchHit("Leaving the house", 0, 0, "Step: Keys, wallet, card", "event_repeat")),
                ),
                SearchSection(
                    "Done",
                    listOf(SearchHit("Cancel gym trial", 0, 2, "Done Thu 12 Nov", "check_circle")),
                ),
            ),
    )

fun searchRecent(): SearchPresentationState =
    SearchPresentationState(
        mode = SearchVisualMode.Recent,
        recentQueries = listOf("call", "dentist", "project brief", "stamps"),
    )

fun searchNoResults(): SearchPresentationState =
    SearchPresentationState(
        query = "zztop",
        mode = SearchVisualMode.NoResults,
    )
