@file:Suppress("NewLineAtEndOfFile")

package app.ebbplanner.ui.donehistory

data class DoneItem(
    val title: String,
    val time: String,
    val restore: Boolean = false,
    val subtitle: String? = null,
    val icon: String? = null,
)

data class DoneSection(
    val label: String,
    val items: List<DoneItem>,
)

data class DoneHistoryPresentationState(
    val filters: List<String> = listOf("All", "Tasks", "Routines", "Let go"),
    val selectedFilter: Int = 0,
    val sections: List<DoneSection> = emptyList(),
    val mode: DoneHistoryMode = DoneHistoryMode.Populated,
)

enum class DoneHistoryMode {
    Empty,
    Populated,
}
