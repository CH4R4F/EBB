@file:Suppress("NewLineAtEndOfFile")

package app.ebbplanner.ui.inbox

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ebbplanner.R
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class InboxViewModel
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        taskRepository: TaskRepository,
    ) : ViewModel() {
        val tasks: StateFlow<List<TaskEntity>> =
            taskRepository.observeInboxTasks()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

        val presentation: StateFlow<InboxPresentationState> =
            taskRepository.observeInboxTasks()
                .onStart { emit(emptyList()) }
                .map { list -> inboxPresentationFromTasks(list) }
                .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5_000),
                    InboxPresentationState(visualMode = InboxVisualMode.Loading, tasks = emptyList()),
                )

        private fun inboxPresentationFromTasks(tasks: List<TaskEntity>): InboxPresentationState {
            if (tasks.isEmpty()) {
                return InboxPresentationState(
                    subtitle = context.resources.getQuantityString(R.plurals.inbox_thought_count, 0, 0),
                    tasks = emptyList(),
                    visualMode = InboxVisualMode.Empty,
                    emptyMessage = context.getString(R.string.inbox_empty_message),
                )
            }
            val subtitle = context.resources.getQuantityString(R.plurals.inbox_thought_count, tasks.size, tasks.size)
            return InboxPresentationState(
                subtitle = subtitle,
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
                visualMode = InboxVisualMode.Normal,
            )
        }
    }
