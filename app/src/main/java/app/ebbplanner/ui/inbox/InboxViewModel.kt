@file:Suppress("NewLineAtEndOfFile")

package app.ebbplanner.ui.inbox

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ebbplanner.R
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.preferences.UserPreferencesRepository
import app.ebbplanner.data.repository.TaskRepository
import app.ebbplanner.domain.capture.QuickCaptureParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class InboxViewModel
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val taskRepository: TaskRepository,
        private val userPreferencesRepository: UserPreferencesRepository,
    ) : ViewModel() {
        val tasks: StateFlow<List<TaskEntity>> =
            taskRepository.observeInboxTasks()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

        val presentation: StateFlow<InboxPresentationState> =
            taskRepository.observeInboxTasks()
                .map { list -> inboxPresentationFromTasks(list) }
                .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5_000),
                    InboxPresentationState(visualMode = InboxVisualMode.Loading, tasks = emptyList()),
                )

        fun captureToInbox(raw: String) {
            if (raw.isBlank()) return
            viewModelScope.launch {
                val parsed = QuickCaptureParser.parse(raw, LocalDate.now())
                val date = parsed.targetDate ?: LocalDate.now()
                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                val start = parsed.startTime?.format(timeFormatter)
                val placement =
                    when {
                        start != null -> TaskEntity.PLACEMENT_SCHEDULED
                        parsed.targetDate != null -> TaskEntity.PLACEMENT_ANYTIME
                        else -> TaskEntity.PLACEMENT_INBOX
                    }
                val remindersOn = userPreferencesRepository.remindersDefault.first()
                taskRepository.addQuickCapture(
                    title = parsed.title,
                    date = if (placement == TaskEntity.PLACEMENT_INBOX) date else date,
                    placement = placement,
                    startLocalTime = start,
                    durationMin = parsed.durationMin ?: TaskEntity.DEFAULT_DURATION_MIN,
                    reminderEnabled =
                        if (placement == TaskEntity.PLACEMENT_SCHEDULED && start != null) {
                            remindersOn
                        } else {
                            false
                        },
                )
            }
        }

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
