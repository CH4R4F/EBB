@file:Suppress("NewLineAtEndOfFile")

package app.ebbplanner.ui.inbox

data class InboxTaskRow(
    val id: String,
    val title: String,
    val colorKey: String,
    val iconKey: String,
    val estimate: String,
    val durationMin: Int = 30,
    val energyLevel: Int = 2,
    val hasReminder: Boolean = false,
    val checklist: String? = null,
    val done: Boolean = false,
    val selected: Boolean = false,
)

data class InboxSwipeReveal(
    val taskId: String,
    val label: String,
    val icon: String,
    val side: InboxSwipeSide,
)

enum class InboxSwipeSide {
    Left,
    Right,
}

data class InboxPresentationState(
    val title: String = "Inbox",
    val subtitle: String = "12 thoughts",
    val filters: List<String> = listOf("All", "Quick ≤15 min"),
    val selectedFilter: Int = 0,
    val searchQuery: String = "",
    val searchVisible: Boolean = false,
    val tasks: List<InboxTaskRow> = emptyList(),
    val visualMode: InboxVisualMode = InboxVisualMode.Normal,
    val swipeReveal: InboxSwipeReveal? = null,
    val dragToTodayHint: String? = null,
    val emptyMessage: String? = null,
    val showFab: Boolean = true,
)

enum class InboxVisualMode {
    Normal,
    Empty,
    FilteredEmpty,
    Many,
    Loading,
    Multiselect,
    SwipeLeft,
    SwipeRight,
    DragToToday,
    TidyUp,
}
