package app.ebbplanner.ui.taskdetail

import java.time.LocalDate

data class ScheduleSuggestion(
    val label: String,
    val subtitle: String? = null,
    val placement: String,
    val date: LocalDate?,
    val startLocalTime: String?,
)