package app.ebbplanner.domain.day

enum class OverlapResolutionType {
    PUSH_LATER,
    SHORTEN_EARLIER,
    NEXT_FREE_SLOT,
    KEEP_BOTH,
}

data class OverlapOption(
    val type: OverlapResolutionType,
    val title: String,
    val subtitle: String,
)

data class OverlapSituation(
    val headline: String,
    val detail: String,
    val firstTitle: String,
    val secondTitle: String,
    val firstStartMinutes: Int,
    val firstDurationMin: Int,
    val secondStartMinutes: Int,
    val secondDurationMin: Int,
    val options: List<OverlapOption>,
)

data class FitMoveSuggestion(
    val taskId: String,
    val title: String,
    val durationMin: Int,
    val reason: String,
    val selected: Boolean,
)

data class MakeItFitPlan(
    val plannedMinutes: Int,
    val availableMinutes: Int,
    val afterPlannedMinutes: Int,
    val moves: List<FitMoveSuggestion>,
)

data class ScheduleDayResult(
    val placedCount: Int,
    val unplacedTaskIds: List<String>,
)

data class ShiftPreviewBlock(
    val id: String,
    val title: String,
    val colorKey: String,
    val startMinutes: Int,
    val durationMin: Int,
    val pinned: Boolean,
    val isEvent: Boolean,
)
