@file:Suppress("NewLineAtEndOfFile")

package app.ebbplanner.ui.routines

data class RoutineCard(
    val id: String,
    val title: String,
    val schedule: String,
    val meta: String,
    val icon: String,
    val running: Boolean = false,
    val stepLabel: String? = null,
    val progress: Float = 0f,
)

data class RecurringTaskRow(
    val title: String,
    val schedule: String,
    val icon: String,
)

data class RoutinesPresentationState(
    val routines: List<RoutineCard> = emptyList(),
    val recurring: List<RecurringTaskRow> = emptyList(),
    val mode: RoutinesVisualMode = RoutinesVisualMode.Populated,
)

enum class RoutinesVisualMode {
    Empty,
    Populated,
    Running,
}
