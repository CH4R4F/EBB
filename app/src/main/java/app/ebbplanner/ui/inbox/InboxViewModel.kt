package app.ebbplanner.ui.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class InboxViewModel
    @Inject
    constructor(
        taskRepository: TaskRepository,
    ) : ViewModel() {
        val tasks: StateFlow<List<TaskEntity>> =
            taskRepository.observeInboxTasks()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

        val presentation: StateFlow<InboxPresentationState> =
            tasks.map { list -> inboxPresentationFromTasks(list) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), inboxPopulated())

        private fun inboxPresentationFromTasks(tasks: List<TaskEntity>): InboxPresentationState =
            inboxPopulated().copy(
                tasks =
                    tasks.map {
                        InboxTaskRow(
                            id = it.id,
                            title = it.title,
                            estimate = "${it.durationMin} min",
                            colorKey = it.colorKey,
                            iconKey = it.iconKey ?: "task",
                        )
                    },
            )
    }
