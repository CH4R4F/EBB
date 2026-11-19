@file:Suppress("NewLineAtEndOfFile")

package app.ebbplanner.ui.inbox

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ebbplanner.R
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.preferences.UserPreferencesRepository
import app.ebbplanner.data.repository.TaskRepository
import app.ebbplanner.domain.capture.QuickCaptureDraft
import app.ebbplanner.domain.capture.QuickCaptureResolved
import app.ebbplanner.domain.capture.resolveQuickCapture
import app.ebbplanner.ui.today.QuickCaptureSaveTarget
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class InboxViewModel
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val taskRepository: TaskRepository,
        private val userPreferencesRepository: UserPreferencesRepository,
    ) : ViewModel() {
        private val searchQuery = MutableStateFlow("")
        private val selectedFilter = MutableStateFlow(0)
        private val searchVisible = MutableStateFlow(false)

        val presentation: StateFlow<InboxPresentationState> =
            combine(
                taskRepository.observeInboxTasks(),
                searchQuery,
                selectedFilter,
                searchVisible,
            ) { tasks, query, filterIndex, searchOn ->
                inboxPresentationFromTasks(tasks, query, filterIndex, searchOn)
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                InboxPresentationState(visualMode = InboxVisualMode.Loading, tasks = emptyList()),
            )

        fun setSearchQuery(query: String) {
            searchQuery.value = query
        }

        fun setFilterIndex(index: Int) {
            selectedFilter.value = index
        }

        fun toggleSearch() {
            searchVisible.update { !it }
            if (!searchVisible.value) searchQuery.value = ""
        }

        fun captureToInbox(
            draft: QuickCaptureDraft,
            onSaved: (TaskEntity, QuickCaptureResolved) -> Unit = { _, _ -> },
        ) {
            if (draft.raw.isBlank()) return
            viewModelScope.launch {
                val resolved = resolveQuickCapture(draft, LocalDate.now(), QuickCaptureSaveTarget.Inbox)
                if (resolved.title.isBlank()) return@launch
                val remindersOn = userPreferencesRepository.remindersDefault.first()
                val task =
                    taskRepository.addQuickCapture(
                        title = resolved.title,
                        date = resolved.date,
                        placement = resolved.placement,
                        startLocalTime = resolved.startLocalTime,
                        durationMin = resolved.durationMin,
                        reminderEnabled =
                            resolved.placement == TaskEntity.PLACEMENT_SCHEDULED &&
                                resolved.startLocalTime != null &&
                                remindersOn,
                    )
                onSaved(task, resolved)
            }
        }

        private fun inboxPresentationFromTasks(
            tasks: List<TaskEntity>,
            query: String,
            filterIndex: Int,
            searchVisible: Boolean,
        ): InboxPresentationState {
            val filtered =
                tasks
                    .filter { task -> query.isBlank() || task.title.contains(query, ignoreCase = true) }
                    .filter { task ->
                        when (filterIndex) {
                            1 -> task.durationMin <= 15
                            else -> true
                        }
                    }
            if (tasks.isEmpty()) {
                return InboxPresentationState(
                    subtitle = context.resources.getQuantityString(R.plurals.inbox_thought_count, 0, 0),
                    tasks = emptyList(),
                    visualMode = InboxVisualMode.Empty,
                    emptyMessage = context.getString(R.string.inbox_empty_message),
                    searchQuery = query,
                    searchVisible = searchVisible,
                    selectedFilter = filterIndex,
                )
            }
            val subtitle = context.resources.getQuantityString(R.plurals.inbox_thought_count, filtered.size, filtered.size)
            return InboxPresentationState(
                subtitle = subtitle,
                tasks =
                    filtered.map {
                        InboxTaskRow(
                            id = it.id,
                            title = it.title,
                            estimate = "${it.durationMin} min",
                            colorKey = it.colorKey,
                            iconKey = it.iconKey ?: "task",
                            durationMin = it.durationMin,
                        )
                    },
                visualMode =
                    when {
                        filtered.isEmpty() && query.isNotBlank() -> InboxVisualMode.FilteredEmpty
                        filtered.isEmpty() && filterIndex > 0 -> InboxVisualMode.FilteredEmpty
                        else -> InboxVisualMode.Normal
                    },
                emptyMessage =
                    when {
                        filtered.isEmpty() && query.isNotBlank() ->
                            context.getString(R.string.inbox_search_empty_message)
                        filtered.isEmpty() && filterIndex > 0 ->
                            context.getString(R.string.inbox_filter_empty_message)
                        else -> null
                    },
                searchQuery = query,
                searchVisible = searchVisible,
                selectedFilter = filterIndex,
                filters = listOf("All", "Quick ≤15 min"),
            )
        }
    }
