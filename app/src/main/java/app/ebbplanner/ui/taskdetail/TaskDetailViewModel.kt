package app.ebbplanner.ui.taskdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel
    @Inject
    constructor(
        private val taskRepository: TaskRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(taskDetailExisting())
        val uiState: StateFlow<TaskDetailPresentationState> = _uiState.asStateFlow()

        fun load(taskId: String) {
            viewModelScope.launch {
                val task = taskRepository.getTask(taskId) ?: return@launch
                _uiState.value = task.toDetailState()
            }
        }

        fun markDone() {
            val id = _uiState.value.id
            viewModelScope.launch {
                taskRepository.markTaskDone(id)
                load(id)
            }
        }

        private fun TaskEntity.toDetailState(): TaskDetailPresentationState =
            taskDetailExisting().copy(
                id = id,
                title = title,
                scheduleLabel = startLocalTime?.let { "$it · $durationMin min" } ?: "$durationMin min",
                done = status == TaskEntity.STATUS_DONE,
            )
    }
