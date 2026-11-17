package app.ebbplanner.ui.taskdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ebbplanner.data.local.entity.SubtaskEntity
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel
    @Inject
    constructor(
        private val taskRepository: TaskRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(taskDetailExisting())
        val uiState: StateFlow<TaskDetailPresentationState> = _uiState.asStateFlow()

        private val _openSheet = MutableStateFlow<TaskSheetType?>(null)
        val openSheet: StateFlow<TaskSheetType?> = _openSheet.asStateFlow()

        private var loadedTaskId: String = ""

        fun load(taskId: String) {
            loadedTaskId = taskId
            viewModelScope.launch {
                taskRepository.observeSubtasks(taskId).collect { list ->
                    val task = taskRepository.getTask(taskId) ?: return@collect
                    _uiState.value = task.toDetailState(list)
                }
            }
        }

        fun openSheet(type: TaskSheetType) {
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
            val task = taskRepository.getTask(taskId) ?: return
            val subs = taskRepository.listSubtasks(taskId)
            _uiState.value = task.toDetailState(subs)
        }

        private fun TaskEntity.toDetailState(subtasks: List<SubtaskEntity>): TaskDetailPresentationState {
            val placementLabel =
                when (placement) {
                    TaskEntity.PLACEMENT_INBOX -> "Inbox"
                    TaskEntity.PLACEMENT_ANYTIME -> "Anytime"
                    TaskEntity.PLACEMENT_SOMEDAY -> "Someday"
                    else -> startLocalTime?.let { "Today · $it" } ?: "Scheduled"
                }
            return taskDetailExisting().copy(
                id = id,
                title = title,
                whenLabel = placementLabel,
                dateLabel = date ?: "No date",
                estimate = formatDuration(durationMin),
                scheduleLabel = startLocalTime?.let { "$it · $durationMin min" } ?: "$durationMin min",
                done = status == TaskEntity.STATUS_DONE,
                durationMin = durationMin,
                remindersEnabled = reminderEnabled,
                remindersLabel = if (reminderEnabled) "At start" else "Off",
                colorKey = colorKey,
                colorIconKey = iconKey ?: "task",
                colorLabel = colorKey.replaceFirstChar { it.uppercase() },
                subtasks =
                    subtasks.map {
                        TaskSubtaskRow(
                            title = it.title,
                            estimate = it.durationMin?.let { m -> "$m min" } ?: "—",
                            done = it.done,
                        )
                    },
                subtaskProgress = "${subtasks.count { it.done }}/${subtasks.size}",
            )
        }

        private fun formatDuration(minutes: Int): String =
            when {
                minutes < 60 -> "$minutes min"
                minutes % 60 == 0 -> "${minutes / 60} h"
                else -> "${minutes / 60} h ${minutes % 60} min"
            }
    }
