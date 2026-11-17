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
import java.util.UUID
import javax.inject.Inject

data class RoutineEditorState(
    val id: String = "",
    val title: String = "",
    val repeatRule: String = RoutineEntity.REPEAT_WEEKDAYS,
    val weeklyDays: Set<Int> = setOf(1, 2, 3, 4, 5),
    val startLocalTime: String = "09:00",
    val durationMin: Int = 30,
    val isNew: Boolean = true,
)

@HiltViewModel
class RoutineEditorViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val routineRepository: RoutineRepository,
    ) : ViewModel() {
        private val routineId: String = savedStateHandle.get<String>("routineId").orEmpty()

        private val _state = MutableStateFlow(RoutineEditorState())
        val state: StateFlow<RoutineEditorState> = _state.asStateFlow()

        init {
            if (routineId.isNotBlank() && routineId != NEW_ROUTINE_ID) {
                viewModelScope.launch {
                    val entity = routineRepository.getRoutine(routineId) ?: return@launch
                    _state.value =
                        RoutineEditorState(
                            id = entity.id,
                            title = entity.title,
                            repeatRule = entity.repeatRule,
                            weeklyDays = parseWeeklyDays(entity.weeklyDays),
                            startLocalTime = entity.startLocalTime,
                            durationMin = entity.durationMin,
                            isNew = false,
                        )
                }
            } else {
                _state.value = RoutineEditorState(id = routineId, isNew = true)
            }
        }

        fun setTitle(title: String) {
            _state.value = _state.value.copy(title = title)
        }

        fun setRepeatRule(rule: String) {
            _state.value = _state.value.copy(repeatRule = rule)
        }

        fun toggleWeeklyDay(day: Int) {
            val days = _state.value.weeklyDays.toMutableSet()
            if (days.contains(day)) days.remove(day) else days.add(day)
            _state.value = _state.value.copy(weeklyDays = days)
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
                val id =
                    if (draft.isNew || draft.id == NEW_ROUTINE_ID) {
                        UUID.randomUUID().toString()
                    } else {
                        draft.id
                    }
                val existing = if (!draft.isNew) routineRepository.getRoutine(draft.id) else null
                val entity =
                    RoutineEntity(
                        id = id,
                        title = draft.title.ifBlank { "Routine" },
                        repeatRule = draft.repeatRule,
                        weeklyDays = encodeWeeklyDays(draft.weeklyDays),
                        startLocalTime = draft.startLocalTime,
                        durationMin = draft.durationMin,
                        createdAt = existing?.createdAt ?: now,
                        updatedAt = now,
                    )
                routineRepository.upsert(entity)
                onSaved()
            }
        }

        fun delete(onDeleted: () -> Unit) {
            if (_state.value.isNew) {
                onDeleted()
                return
            }
            viewModelScope.launch {
                routineRepository.delete(_state.value.id)
                onDeleted()
            }
        }

        private fun parseWeeklyDays(raw: String): Set<Int> =
            if (raw.isBlank()) {
                setOf(1, 2, 3, 4, 5)
            } else {
                raw.split(',').mapNotNull { it.trim().toIntOrNull() }.toSet()
            }

        private fun encodeWeeklyDays(days: Set<Int>): String = days.sorted().joinToString(",")

        companion object {
            const val NEW_ROUTINE_ID = "new"
        }
    }