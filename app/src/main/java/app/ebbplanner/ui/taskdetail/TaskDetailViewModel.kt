@file:Suppress("NewLineAtEndOfFile")

package app.ebbplanner.ui.taskdetail

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ebbplanner.data.local.entity.SubtaskEntity
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.preferences.UserPreferencesRepository
import app.ebbplanner.data.repository.TaskRepository
import app.ebbplanner.ui.navigation.AppRoutes
import app.ebbplanner.domain.day.DayGapScheduler
import app.ebbplanner.domain.day.DayPlanningUseCase
import app.ebbplanner.domain.time.EbbClock
import app.ebbplanner.ui.time.ClockFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val taskRepository: TaskRepository,
        private val userPreferencesRepository: UserPreferencesRepository,
        private val dayPlanningUseCase: DayPlanningUseCase,
        private val clock: EbbClock,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(TaskDetailPresentationState(mode = TaskDetailMode.Loading))
        val uiState: StateFlow<TaskDetailPresentationState> = _uiState.asStateFlow()

        private val _openSheet = MutableStateFlow<TaskSheetType?>(null)
        val openSheet: StateFlow<TaskSheetType?> = _openSheet.asStateFlow()

        private var loadedTaskId: String = ""
        private var defaultBreatherMinutes: Int = 5

        init {
            viewModelScope.launch {
                defaultBreatherMinutes = userPreferencesRepository.breatherMinutes.first()
            }
        }

        fun load(taskId: String) {
            loadedTaskId = taskId
            _uiState.value = TaskDetailPresentationState(mode = TaskDetailMode.Loading)
            viewModelScope.launch {
                defaultBreatherMinutes = userPreferencesRepository.breatherMinutes.first()
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
                    val suggestions = buildScheduleSuggestions(latest)
                    _uiState.value = latest.toDetailState(list, suggestions)
                }
            }
        }

        fun openSheet(type: TaskSheetType) {
            if (_uiState.value.mode == TaskDetailMode.NotFound) return
            if (type == TaskSheetType.Schedule) {
                viewModelScope.launch { refreshScheduleSuggestions() }
            }
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

        fun deleteTask() {
            val id = _uiState.value.id
            if (id.isBlank()) return
            viewModelScope.launch {
                taskRepository.deleteTask(id)
            }
        }

        fun toggleBreatherAfter() {
            viewModelScope.launch {
                val task = taskRepository.getTask(loadedTaskId) ?: return@launch
                if (task.breatherBufferMin > 0) {
                    taskRepository.updateTaskFields(loadedTaskId) {
                        it.copy(
                            durationMin = (it.durationMin - it.breatherBufferMin).coerceAtLeast(5),
                            breatherBufferMin = 0,
                        )
                    }
                } else {
                    taskRepository.updateTaskFields(loadedTaskId) {
                        it.copy(
                            durationMin = it.durationMin + defaultBreatherMinutes,
                            breatherBufferMin = defaultBreatherMinutes,
                        )
                    }
                }
                reloadTask(loadedTaskId)
            }
        }

        fun navigateTargetForRepeat(onResult: (String) -> Unit) {
            viewModelScope.launch {
                val task = taskRepository.getTask(loadedTaskId) ?: return@launch
                val route =
                    task.sourceRoutineId?.takeIf { it.isNotBlank() }?.let {
                        AppRoutes.routineEdit(it)
                    } ?: AppRoutes.routineEditFromTask(loadedTaskId)
                onResult(route)
            }
        }

        fun setPinned(pinned: Boolean) {
            viewModelScope.launch {
                taskRepository.updateTaskFields(loadedTaskId) { it.copy(pinned = pinned) }
                reloadTask(loadedTaskId)
            }
        }

        fun setEnergy(level: Int) {
            val energy =
                when (level) {
                    1 -> "low"
                    3 -> "high"
                    else -> "med"
                }
            viewModelScope.launch {
                taskRepository.updateTaskFields(loadedTaskId) { it.copy(energy = energy) }
                reloadTask(loadedTaskId)
            }
        }

        fun adjustDuration(deltaMinutes: Int) {
            val current = _uiState.value.durationMin
            val next = (current + deltaMinutes).coerceAtLeast(5)
            saveDuration(next)
        }

        fun toggleSubtask(subtaskId: String) {
            if (subtaskId.isBlank()) return
            viewModelScope.launch {
                taskRepository.toggleSubtask(subtaskId)
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

        private suspend fun refreshScheduleSuggestions() {
            val task = taskRepository.getTask(loadedTaskId) ?: return
            val suggestions = buildScheduleSuggestions(task)
            _uiState.value = _uiState.value.copy(scheduleSuggestions = suggestions)
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
            val suggestions = buildScheduleSuggestions(task)
            _uiState.value = task.toDetailState(subs, suggestions)
        }

        private suspend fun buildScheduleSuggestions(task: TaskEntity): List<ScheduleSuggestion> {
            val today = clock.today()
            val tomorrow = today.plusDays(1)
            val nowMinutes = clock.nowMinutes()
            val bedMinutes = userPreferencesRepository.dayBedMinutes.first()
            val wakeMinutes = userPreferencesRepository.dayWakeMinutes.first()
            val scheduled = dayPlanningUseCase.scheduledOpenTasks(today)
            val busy =
                DayGapScheduler.busyFromScheduled(
                    scheduled
                        .filter { it.id != task.id }
                        .mapNotNull { t ->
                            val start = ClockFormat.parseMinutes(t.startLocalTime ?: return@mapNotNull null) ?: return@mapNotNull null
                            Triple(t.id, start, t.durationMin)
                        },
                )
            val cursor = maxOf(DayGapScheduler.roundUpToFive(nowMinutes), wakeMinutes)
            val freeStart =
                DayGapScheduler.findSlotPublic(
                    cursor,
                    task.durationMin,
                    busy,
                    bedMinutes,
                )
            val suggestions = mutableListOf<ScheduleSuggestion>()
            suggestions +=
                ScheduleSuggestion(
                    label = "Anytime today",
                    subtitle = "In the Anytime tray",
                    placement = TaskEntity.PLACEMENT_ANYTIME,
                    date = today,
                    startLocalTime = null,
                )
            freeStart?.let { start ->
                val clockLabel = ClockFormat.minutesToClockDisplay(start, ClockFormat.isSystem24Hour(context))
                suggestions +=
                    ScheduleSuggestion(
                        label = "Today · $clockLabel",
                        subtitle = "Next free slot",
                        placement = TaskEntity.PLACEMENT_SCHEDULED,
                        date = today,
                        startLocalTime = ClockFormat.minutesToLocalTime(start),
                    )
            }
            suggestions +=
                ScheduleSuggestion(
                    label = "Tomorrow · Anytime",
                    subtitle = null,
                    placement = TaskEntity.PLACEMENT_ANYTIME,
                    date = tomorrow,
                    startLocalTime = null,
                )
            suggestions +=
                ScheduleSuggestion(
                    label = "Inbox",
                    subtitle = "No date",
                    placement = TaskEntity.PLACEMENT_INBOX,
                    date = null,
                    startLocalTime = null,
                )
            suggestions +=
                ScheduleSuggestion(
                    label = "Someday",
                    subtitle = null,
                    placement = TaskEntity.PLACEMENT_SOMEDAY,
                    date = null,
                    startLocalTime = null,
                )
            return suggestions
        }

        @Suppress("CyclomaticComplexMethod")
        private fun TaskEntity.toDetailState(
            subtasks: List<SubtaskEntity>,
            scheduleSuggestions: List<ScheduleSuggestion>,
        ): TaskDetailPresentationState {
            val taskDate = date?.let { LocalDate.parse(it) }
            val relativeWhen = relativeDayLabel(taskDate)
            val (whenLabel, dateLabel) =
                when (placement) {
                    TaskEntity.PLACEMENT_INBOX ->
                        "Inbox" to "No date"
                    TaskEntity.PLACEMENT_SOMEDAY ->
                        "Someday" to "No date"
                    TaskEntity.PLACEMENT_ANYTIME ->
                        (relativeWhen ?: "Anytime") to "Anytime"
                    else -> {
                        val timeLine =
                            startLocalTime?.let { start ->
                                val is24Hour = ClockFormat.isSystem24Hour(context)
                                val startMin = ClockFormat.parseMinutes(start) ?: 0
                                ClockFormat.rangeDisplay(startMin, startMin + durationMin, is24Hour)
                            }
                        (relativeWhen ?: "Scheduled") to (timeLine ?: "Scheduled")
                    }
                }
            val scheduleText =
                startLocalTime?.let { start ->
                    "${ClockFormat.localTimeToClockDisplay(start, ClockFormat.isSystem24Hour(context))} · $durationMin min"
                } ?: "$durationMin min"
            val breatherSubtitle =
                if (breatherBufferMin > 0) {
                    "$breatherBufferMin min buffer on · tap to remove"
                } else {
                    "Add $defaultBreatherMinutes min buffer · tap to apply"
                }
            val repeatLabel =
                if (!sourceRoutineId.isNullOrBlank()) {
                    "From routine"
                } else {
                    "Doesn't repeat"
                }
            return TaskDetailPresentationState(
                id = id,
                title = title,
                whenLabel = whenLabel,
                dateLabel = dateLabel,
                estimate = formatDuration(durationMin),
                scheduleLabel = scheduleText,
                done = status == TaskEntity.STATUS_DONE,
                pinned = pinned,
                energySelected = energy.toEnergyLevel(),
                durationMin = durationMin,
                remindersEnabled = reminderEnabled,
                remindersLabel = if (reminderEnabled) "At start" else "Off",
                colorKey = colorKey,
                colorIconKey = iconKey ?: "task",
                colorLabel = colorKey.replaceFirstChar { it.uppercase() },
                notes = notes,
                repeatLabel = repeatLabel,
                breatherSubtitle = breatherSubtitle,
                subtasks =
                    subtasks.map {
                        TaskSubtaskRow(
                            id = it.id,
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
                scheduleSuggestions = scheduleSuggestions,
                scheduledDate = taskDate,
                scheduledStartLocalTime = startLocalTime,
            )
        }

        private fun relativeDayLabel(date: LocalDate?): String? {
            if (date == null) return null
            val today = clock.today()
            return when {
                date == today -> "Today"
                date == today.plusDays(1) -> "Tomorrow"
                else -> date.format(DateTimeFormatter.ofPattern("EEE d MMM"))
            }
        }

        private fun String?.toEnergyLevel(): Int =
            when (this) {
                "low" -> 1
                "high" -> 3
                "med", "medium" -> 2
                else -> 2
            }

        private fun formatDuration(minutes: Int): String =
            when {
                minutes < 60 -> "$minutes min"
                minutes % 60 == 0 -> "${minutes / 60} h"
                else -> "${minutes / 60} h ${minutes % 60} min"
            }
    }