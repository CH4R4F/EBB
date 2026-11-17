package app.ebbplanner.ui.routines

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ebbplanner.data.local.entity.RoutineEntity
import app.ebbplanner.data.repository.RoutineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoutineEditorState(
    val id: String = "",
    val title: String = "",
    val repeatRule: String = RoutineEntity.REPEAT_WEEKDAYS,
    val startLocalTime: String = "09:00",
    val durationMin: Int = 30,
)

@HiltViewModel
class RoutineEditorViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val routineRepository: RoutineRepository,
    ) : ViewModel() {
        private val routineId: String = savedStateHandle.get<String>("routineId").orEmpty()

        private val _state = MutableStateFlow(RoutineEditorState(id = routineId))
        val state: StateFlow<RoutineEditorState> = _state.asStateFlow()

        init {
            if (routineId.isNotBlank()) {
                viewModelScope.launch {
                    val entity = routineRepository.getRoutine(routineId) ?: return@launch
                    _state.value =
                        RoutineEditorState(
                            id = entity.id,
                            title = entity.title,
                            repeatRule = entity.repeatRule,
                            startLocalTime = entity.startLocalTime,
                            durationMin = entity.durationMin,
                        )
                }
            }
        }

        fun setTitle(title: String) {
            _state.value = _state.value.copy(title = title)
        }

        fun setRepeatRule(rule: String) {
            _state.value = _state.value.copy(repeatRule = rule)
        }

        fun setStartTime(time: String) {
            _state.value = _state.value.copy(startLocalTime = time)
        }

        fun setDuration(minutes: Int) {
            _state.value = _state.value.copy(durationMin = minutes)
        }

        fun save(onSaved: () -> Unit) {
            viewModelScope.launch {
                val draft = _state.value
                val now = System.currentTimeMillis()
                val existing = if (draft.id.isNotBlank()) routineRepository.getRoutine(draft.id) else null
                val entity =
                    RoutineEntity(
                        id = existing?.id ?: draft.id.ifBlank { java.util.UUID.randomUUID().toString() },
                        title = draft.title.ifBlank { "Routine" },
                        repeatRule = draft.repeatRule,
                        weeklyDays = "",
                        startLocalTime = draft.startLocalTime,
                        durationMin = draft.durationMin,
                        createdAt = existing?.createdAt ?: now,
                        updatedAt = now,
                    )
                routineRepository.upsert(entity)
                onSaved()
            }
        }
    }
