@file:Suppress("NewLineAtEndOfFile")

package app.ebbplanner.ui.taskdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ebbplanner.data.local.entity.SubtaskEntity
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.repository.TaskRepository
import app.ebbplanner.ui.time.ClockFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel
    @Inject
    constructor(
        private val taskRepository: TaskRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(TaskDetailPresentationState(mode = TaskDetailMode.Loading))
        val uiState: StateFlow<TaskDetailPresentationState> = _uiState.asStateFlow()

        private val _openSheet = MutableStateFlow<TaskSheetType?>(null)
        val openSheet: StateFlow<TaskSheetType?> = _openSheet.asStateFlow()

        private var loadedTaskId: String = ""

        fun load(taskId: String) {
            loadedTaskId = taskId
            _uiState.value = TaskDetailPresentationState(mode = TaskDetailMode.Loading)
            viewModelScope.launch {
                val task = taskRepository.getTask(taskId)
                if (task == null) {
                    _uiState.value =
                        TaskDetailPresentationState(
                            id = taskId,
                            mode = TaskDetailMode.NotFound,
                        )
                    return@launch
                }
                taskRepository.observeSubtasks(taskId).collect { list ->
                    val latest = taskRepository.getTask(taskId)
                    if (latest == null) {
                        _uiState.value =
                            TaskDetailPresentationState(
                                id = taskId,
                                mode = TaskDetailMode.NotFound,
                            )
                        return@collect
                    }
                    _uiState.value = latest.toDetailState(list)
                }
            }
        }

        fun openSheet(type: TaskSheetType) {
            if (_uiState.value.mode == TaskDetailMode.NotFound) return
            _openSheet.value = type
        }

        fun openSheetFromRoute(arg: String?) {
            if (arg.isNullOrBlank() || arg == "none") return
            TaskSheetType.fromRoute(arg)?.let { openSheet(it) }
        }

        fun dismissSheet() {
            _openSheet.value = null
        }

        fun markDone() {
            val id = _uiState.value.id
            if (id.isBlank() || _uiState.value.mode == TaskDetailMode.NotFound) return
            viewModelScope.launch {
                taskRepository.markTaskDone(id)
                reloadTask(id)
            }
        }

        fun saveReminder(enabled: Boolean) {
            viewModelScope.launch {
                taskRepository.updateTaskFields(loadedTaskId) { it.copy(reminderEnabled = enabled) }
                reloadTask(loadedTaskId)
            }
        }

        fun saveDuration(minutes: Int) {
            viewModelScope.launch {
                taskRepository.updateTaskFields(loadedTaskId) { it.copy(durationMin = minutes) }
                reloadTask(loadedTaskId)
            }
        }

        fun addSubtask(title: String) {
            viewModelScope.launch {
                taskRepository.addSubtask(loadedTaskId, title)
            }
        }

        fun saveSchedule(
            placement: String,
            date: LocalDate?,
            startTime: String?,
        ) {
            viewModelScope.launch {
                taskRepository.scheduleTask(loadedTaskId, placement, date, startTime)
                reloadTask(loadedTaskId)
            }
        }

        fun saveColorIcon(
            colorKey: String,
            iconKey: String,
        ) {
            viewModelScope.launch {
                taskRepository.updateTaskFields(loadedTaskId) {
                    it.copy(colorKey = colorKey, iconKey = iconKey)
                }
                reloadTask(loadedTaskId)
            }
        }

        private suspend fun reloadTask(taskId: String) {
            val task = taskRepository.getTask(taskId)
            if (task == null) {
                _uiState.value =
                    TaskDetailPresentationState(
                        id = taskId,
                        mode = TaskDetailMode.NotFound,
                    )
                return
            }
            val subs = taskRepository.listSubtasks(taskId)
            _uiState.value = task.toDetailState(subs)
        }

        @Suppress("CyclomaticComplexMethod")
        private fun TaskEntity.toDetailState(subtasks: List<SubtaskEntity>): TaskDetailPresentationState {
            val placementLabel =
                when (placement) {
                    TaskEntity.PLACEMENT_INBOX -> "Inbox"
                    TaskEntity.PLACEMENT_ANYTIME -> "Anytime"
                    TaskEntity.PLACEMENT_SOMEDAY -> "Someday"
                    else ->
                        startLocalTime?.let { start ->
                            val endMin =
                                (ClockFormat.parseMinutes(start) ?: 0) + durationMin
                            "Today · ${ClockFormat.localTimeToClock(start)}–${ClockFormat.minutesToClock(endMin)}"
                        } ?: "Scheduled"
                }
            val dateLabelText =
                date?.let { LocalDate.parse(it).format(DateTimeFormatter.ofPattern("EEE d MMM")) }
                    ?: "No date"
            val scheduleText =
                startLocalTime?.let { start ->
                    "${ClockFormat.localTimeToClock(start)} · $durationMin min"
                } ?: "$durationMin min"
            return TaskDetailPresentationState(
                id = id,
                title = title,
                whenLabel = placementLabel,
                dateLabel = dateLabelText,
                estimate = formatDuration(durationMin),
                scheduleLabel = scheduleText,
                done = status == TaskEntity.STATUS_DONE,
                pinned = pinned,
                durationMin = durationMin,
                remindersEnabled = reminderEnabled,
                remindersLabel = if (reminderEnabled) "At start" else "Off",
                colorKey = colorKey,
                colorIconKey = iconKey ?: "task",
                colorLabel = colorKey.replaceFirstChar { it.uppercase() },
                notes = notes,
                subtasks =
                    subtasks.map {
                        TaskSubtaskRow(
                            title = it.title,
                            estimate = it.durationMin?.let { m -> "$m min" } ?: "—",
                            done = it.done,
                        )
                    },
                subtaskProgress =
                    if (subtasks.isEmpty()) {
                        "0/0"
                    } else {
                        "${subtasks.count { it.done }}/${subtasks.size}"
                    },
                mode = if (status == TaskEntity.STATUS_DONE) TaskDetailMode.Done else TaskDetailMode.Existing,
            )
        }

        private fun formatDuration(minutes: Int): String =
            when {
                minutes < 60 -> "$minutes min"
                minutes % 60 == 0 -> "${minutes / 60} h"
                else -> "${minutes / 60} h ${minutes % 60} min"
            }
    }