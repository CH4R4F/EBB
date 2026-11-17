package app.ebbplanner.ui.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ebbplanner.data.local.entity.RoutineEntity
import app.ebbplanner.data.repository.RoutineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoutinesViewModel
    @Inject
    constructor(
        private val routineRepository: RoutineRepository,
    ) : ViewModel() {
        val presentation: StateFlow<RoutinesPresentationState> =
            routineRepository
                .observeRoutines()
                .map { entities -> entities.toPresentation() }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), routinesEmpty())

        fun createRoutine(onCreated: (String) -> Unit) {
            viewModelScope.launch {
                val created = routineRepository.createDefault(title = "New routine")
                onCreated(created.id)
            }
        }

        private fun List<RoutineEntity>.toPresentation(): RoutinesPresentationState {
            if (isEmpty()) return routinesEmpty()
            val cards =
                map { entity ->
                    RoutineCard(
                        title = entity.title,
                        schedule = formatSchedule(entity),
                        meta = "${entity.durationMin} min",
                        icon = entity.iconKey ?: "event_repeat",
                    )
                }
            return RoutinesPresentationState(routines = cards, recurring = emptyList())
        }

        private fun formatSchedule(entity: RoutineEntity): String {
            val rule =
                when (entity.repeatRule) {
                    RoutineEntity.REPEAT_DAILY -> "Daily"
                    RoutineEntity.REPEAT_WEEKDAYS -> "Weekdays"
                    RoutineEntity.REPEAT_WEEKLY -> "Weekly"
                    else -> entity.repeatRule
                }
            return "$rule · ${entity.startLocalTime}"
        }
    }
