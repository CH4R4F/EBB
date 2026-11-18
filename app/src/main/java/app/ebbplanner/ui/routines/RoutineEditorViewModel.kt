package app.ebbplanner.ui.routines

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ebbplanner.data.local.entity.RoutineEntity
import app.ebbplanner.data.repository.RoutineRepository
import app.ebbplanner.data.repository.TaskRepository
import app.ebbplanner.domain.routines.RoutineInstanceGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

data class RoutineEditorState(
    val id: String = "",
    val title: String = "",
    val repeatRule: String = RoutineEntity.REPEAT_WEEKDAYS,
    val weeklyDays: Set<Int> = setOf(1, 2, 3, 4, 5),
    val startLocalTime: String = "09:00",
    val durationMin: Int = 30,
    val colorKey: String = "slate",
    val iconKey: String? = "event_repeat",
    val enabled: Boolean = true,
    val isNew: Boolean = true,
    val saveInFlight: Boolean = false,
    val saveError: String? = null,
)

@HiltViewModel
class RoutineEditorViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val routineRepository: RoutineRepository,
        private val routineInstanceGenerator: RoutineInstanceGenerator,
        private val taskRepository: TaskRepository,
    ) : ViewModel() {
        private val routineId: String = savedStateHandle.get<String>("routineId").orEmpty()
        private val linkTaskId: String? =
            savedStateHandle.get<String>("linkTaskId")?.takeIf { it.isNotBlank() }

        private val _state = MutableStateFlow(RoutineEditorState())
        val state: StateFlow<RoutineEditorState> = _state.asStateFlow()

        init {
            when {
                routineId.isNotBlank() && routineId != NEW_ROUTINE_ID -> {
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
                                colorKey = entity.colorKey,
                                iconKey = entity.iconKey,
                                enabled = entity.enabled,
                                isNew = false,
                            )
                    }
                }
                linkTaskId != null -> {
                    viewModelScope.launch {
                        val task = taskRepository.getTask(linkTaskId) ?: return@launch
                        _state.value =
                            RoutineEditorState(
                                id = NEW_ROUTINE_ID,
                                title = task.title,
                                repeatRule = RoutineEntity.REPEAT_WEEKDAYS,
                                weeklyDays = setOf(1, 2, 3, 4, 5),
                                startLocalTime = task.startLocalTime ?: "09:00",
                                durationMin = task.durationMin,
                                colorKey = task.colorKey,
                                iconKey = task.iconKey,
                                enabled = true,
                                isNew = true,
                            )
                    }
                }
                else -> {
                    _state.value = RoutineEditorState(id = routineId, isNew = true)
                }
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

        fun save(onSaved: (deferredInstanceWork: Boolean) -> Unit) {
            viewModelScope.launch {
                val draft = _state.value
                if (draft.saveInFlight) return@launch
                if (draft.repeatRule == RoutineEntity.REPEAT_WEEKLY && draft.weeklyDays.isEmpty()) {
                    _state.value = draft.copy(saveError = "Pick at least one weekday")
                    return@launch
                }
                val stableId =
                    when {
                        !draft.isNew && draft.id.isNotBlank() && draft.id != NEW_ROUTINE_ID -> draft.id
                        draft.id.isNotBlank() && draft.id != NEW_ROUTINE_ID -> draft.id
                        else -> UUID.randomUUID().toString()
                    }
                _state.value =
                    draft.copy(
                        id = stableId,
                        isNew = false,
                        saveInFlight = true,
                        saveError = null,
                    )
                val now = System.currentTimeMillis()
                val existing = routineRepository.getRoutine(stableId)
                val entity =
                    RoutineEntity(
                        id = stableId,
                        title = draft.title.ifBlank { "Routine" },
                        repeatRule = draft.repeatRule,
                        weeklyDays = encodeWeeklyDays(draft.repeatRule, draft.weeklyDays),
                        startLocalTime = draft.startLocalTime,
                        durationMin = draft.durationMin,
                        colorKey = draft.colorKey,
                        iconKey = draft.iconKey,
                        enabled = draft.enabled,
                        createdAt = existing?.createdAt ?: now,
                        updatedAt = now,
                    )
                try {
                    val deferred =
                        withContext(NonCancellable) {
                            routineInstanceGenerator.persistRoutineAndSyncInstances(
                                upsertRoutine = { routineRepository.upsert(entity) },
                                routine = entity,
                                linkTaskId = linkTaskId,
                                linkOnSave = linkTaskId != null && existing == null,
                            )
                        }
                    _state.value = _state.value.copy(saveInFlight = false, saveError = null)
                    onSaved(deferred)
                } catch (e: CancellationException) {
                    _state.value = _state.value.copy(saveInFlight = false)
                    throw e
                } catch (t: Throwable) {
                    if (routineRepository.getRoutine(stableId) != null) {
                        _state.value = _state.value.copy(saveInFlight = false, saveError = null)
                        onSaved(false)
                    } else {
                        _state.value =
                            _state.value.copy(
                                saveInFlight = false,
                                saveError = "Could not save routine. Try again.",
                            )
                    }
                }
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
                emptySet()
            } else {
                raw.split(',').mapNotNull { it.trim().toIntOrNull() }.toSet()
            }

        private fun encodeWeeklyDays(
            repeatRule: String,
            days: Set<Int>,
        ): String =
            when (repeatRule) {
                RoutineEntity.REPEAT_WEEKLY -> days.sorted().joinToString(",")
                else -> ""
            }

        companion object {
            const val NEW_ROUTINE_ID = "new"
        }
    }