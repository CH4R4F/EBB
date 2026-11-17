package app.ebbplanner.ui.inbox

data class InboxTaskRow(
    val id: String,
    val title: String,
    val colorKey: String,
    val iconKey: String,
    val estimate: String,
    val energyLevel: Int = 2,
    val hasReminder: Boolean = false,
    val checklist: String? = null,
)

data class InboxPresentationState(
    val title: String = "Inbox",
    val subtitle: String = "12 thoughts",
    val filters: List<String> = listOf("All", "Quick ≤15 min", "Low energy", "Has steps"),
    val selectedFilter: Int = 0,
    val tasks: List<InboxTaskRow> = emptyList(),
    val visualMode: InboxVisualMode = InboxVisualMode.Normal,
)

enum class InboxVisualMode {
    Normal,
    Empty,
    Loading,
    Multiselect,
    SwipeLeft,
    SwipeRight,
    DragToToday,
    TidyUp,
}
