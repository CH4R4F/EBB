package app.ebbplanner.domain.day

import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.repository.TaskRepository
import app.ebbplanner.domain.time.EbbClock
import app.ebbplanner.ui.time.ClockFormat
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DayPlanningUseCase
    @Inject
    constructor(
        private val taskRepository: TaskRepository,
        private val clock: EbbClock,
    ) {
        @Suppress("LongMethod")
        suspend fun analyzeOverlap(
            date: LocalDate,
            bedMinutes: Int,
            is24Hour: Boolean = true,
        ): OverlapSituation? {
            val withStarts = tasksWithStartMinutes(scheduledOpenTasks(date))
            val pair = firstOverlappingPair(withStarts) ?: return null
            val (first, startA) = pair.first
            val (second, startB) = pair.second
            val earlierTask: TaskEntity
            val earlierStart: Int
            val laterTask: TaskEntity
            val laterStart: Int
            if (startA <= startB) {
                earlierTask = first
                earlierStart = startA
                laterTask = second
                laterStart = startB
            } else {
                earlierTask = second
                earlierStart = startB
                laterTask = first
                laterStart = startA
            }
            val earlierEnd = earlierStart + earlierTask.durationMin
            val laterEnd = laterStart + laterTask.durationMin
            val overlapStart = maxOf(earlierStart, laterStart)
            val overlapEnd = minOf(earlierEnd, laterEnd)
            val overlapMin = (overlapEnd - overlapStart).coerceAtLeast(0)
            val pushStart = earlierEnd
            val shortenDuration = laterStart - earlierStart
            val freeSlot =
                findNextFreeSlot(
                    withStarts,
                    earlierEnd,
                    laterTask.durationMin,
                    excludeTaskId = laterTask.id,
                    bedMinutes = bedMinutes,
                )
            val options = mutableListOf<OverlapOption>()
            if (!laterTask.pinned) {
                options +=
                    OverlapOption(
                        OverlapResolutionType.PUSH_LATER,
                        "Push later",
                        "${laterTask.title} starts ${ClockFormat.minutesToClockDisplay(pushStart, is24Hour)}",
                    )
            }
            if (!earlierTask.pinned) {
                options +=
                    OverlapOption(
                        OverlapResolutionType.SHORTEN_EARLIER,
                        "Shorten earlier",
                        "${earlierTask.title} becomes $shortenDuration min",
                    )
            }
            if (!laterTask.pinned) {
                options +=
                    OverlapOption(
                        OverlapResolutionType.NEXT_FREE_SLOT,
                        "Next free slot",
                        freeSlot?.let { "Move ${laterTask.title} to ${ClockFormat.minutesToClockDisplay(it, is24Hour)}" }
                            ?: "No slot found",
                    )
            }
            options +=
                OverlapOption(
                    OverlapResolutionType.KEEP_BOTH,
                    "Keep both",
                    "Fine — I'll juggle",
                )
            return OverlapSituation(
                headline = "These overlap",
                detail = "${earlierTask.title} and ${laterTask.title} share $overlapMin minutes.",
                firstTitle = earlierTask.title,
                secondTitle = laterTask.title,
                firstStartMinutes = earlierStart,
                firstDurationMin = earlierTask.durationMin,
                secondStartMinutes = laterStart,
                secondDurationMin = laterTask.durationMin,
                options = options,
            )
        }

        suspend fun applyOverlap(
            date: LocalDate,
            type: OverlapResolutionType,
            bedMinutes: Int,
        ): Boolean {
            val withStarts = tasksWithStartMinutes(scheduledOpenTasks(date))
            val pair = firstOverlappingPair(withStarts) ?: return false
            val (first, startA) = pair.first
            val (second, startB) = pair.second
            val earlier = if (startA <= startB) first to startA else second to startB
            val later = if (startA <= startB) second to startB else first to startA
            var ok = true
            when (type) {
                OverlapResolutionType.KEEP_BOTH -> ok = true
                OverlapResolutionType.PUSH_LATER -> {
                    if (later.first.pinned) return false
                    val newStart = earlier.second + earlier.first.durationMin
                    val offset = newStart - later.second
                    taskRepository.rescheduleTaskStart(later.first.id, date, later.second, offset)
                }
                OverlapResolutionType.SHORTEN_EARLIER -> {
                    if (earlier.first.pinned) return false
                    val newDuration = (later.second - earlier.second).coerceAtLeast(5)
                    taskRepository.updateTaskFields(earlier.first.id) {
                        it.copy(durationMin = newDuration)
                    }
                }
                OverlapResolutionType.NEXT_FREE_SLOT -> {
                    if (later.first.pinned) return false
                    val slotStart = earlier.second + earlier.first.durationMin
                    val slot =
                        findNextFreeSlot(
                            withStarts,
                            slotStart,
                            later.first.durationMin,
                            excludeTaskId = later.first.id,
                            bedMinutes = bedMinutes,
                        )
                    if (slot == null) {
                        ok = false
                    } else {
                        val offset = slot - later.second
                        taskRepository.rescheduleTaskStart(later.first.id, date, later.second, offset)
                    }
                }
            }
            return ok
        }

        suspend fun resolveFirstOverlapPushLater(
            date: LocalDate,
            bedMinutes: Int,
        ): Boolean = applyOverlap(date, OverlapResolutionType.PUSH_LATER, bedMinutes)

        suspend fun buildMakeItFitPlan(
            date: LocalDate,
            nowMinutes: Int,
            bedMinutes: Int,
        ): MakeItFitPlan {
            val scheduled =
                scheduledOpenTasks(date).filter { task ->
                    val raw = task.startLocalTime ?: return@filter false
                    ClockFormat.parseMinutes(raw) != null
                }
            val planned =
                scheduled
                    .filter { it.status != TaskEntity.STATUS_DONE }
                    .sumOf { it.durationMin }
            val available = (bedMinutes - nowMinutes).coerceAtLeast(0)
            val moves = mutableListOf<FitMoveSuggestion>()
            val victims =
                scheduled
                    .filter { task ->
                        val start = ClockFormat.parseMinutes(task.startLocalTime ?: return@filter false) ?: return@filter false
                        !task.pinned && start >= nowMinutes && task.status != TaskEntity.STATUS_DONE
                    }.sortedByDescending { ClockFormat.parseMinutes(it.startLocalTime ?: "00:00") ?: 0 }
            for (task in victims) {
                if (planned <= available && moves.isEmpty()) break
                moves +=
                    FitMoveSuggestion(
                        taskId = task.id,
                        title = task.title,
                        durationMin = task.durationMin,
                        reason = "Move to Inbox — frees ${task.durationMin} min",
                        selected = true,
                    )
            }
            val defaultSelectedFreed = moves.filter { it.selected }.sumOf { it.durationMin }
            return MakeItFitPlan(
                plannedMinutes = planned,
                availableMinutes = available,
                afterPlannedMinutes = planned - defaultSelectedFreed,
                moves = moves,
            )
        }

        fun afterPlannedMinutes(
            plan: MakeItFitPlan,
            selectedTaskIds: Set<String>,
        ): Int {
            val freed = plan.moves.filter { it.taskId in selectedTaskIds }.sumOf { it.durationMin }
            return (plan.plannedMinutes - freed).coerceAtLeast(0)
        }

        suspend fun applyMakeItFit(
            date: LocalDate,
            nowMinutes: Int,
            bedMinutes: Int,
            selectedTaskIds: Set<String>,
        ): Int {
            val plan = buildMakeItFitPlan(date, nowMinutes, bedMinutes)
            val toMove = plan.moves.filter { it.taskId in selectedTaskIds && it.selected }
            toMove.forEach { move ->
                taskRepository.scheduleTask(move.taskId, TaskEntity.PLACEMENT_INBOX, null, null)
            }
            return toMove.size
        }

        suspend fun makeDayFit(
            date: LocalDate,
            availableMinutes: Int,
        ): Int {
            val now = clock.nowMinutes()
            val plan = buildMakeItFitPlan(date, now, now + availableMinutes)
            return applyMakeItFit(date, now, now + availableMinutes, plan.moves.map { it.taskId }.toSet())
        }

        suspend fun scheduleTasksOnDay(
            date: LocalDate,
            taskIds: List<String>,
            wakeMinutes: Int,
            bedMinutes: Int,
            nowMinutes: Int,
        ): ScheduleDayResult {
            if (taskIds.isEmpty()) return ScheduleDayResult(0, emptyList())
            val scheduled = scheduledOpenTasks(date)
            val busy =
                DayGapScheduler.busyFromScheduled(
                    scheduled.mapNotNull { task ->
                        val raw = task.startLocalTime ?: return@mapNotNull null
                        val start = ClockFormat.parseMinutes(raw) ?: return@mapNotNull null
                        Triple(task.id, start, task.durationMin)
                    },
                )
            val cursor = maxOf(DayGapScheduler.roundUpToFive(nowMinutes), wakeMinutes)
            val durations =
                taskIds.mapNotNull { id ->
                    val task = taskRepository.getTask(id) ?: return@mapNotNull null
                    id to task.durationMin
                }
            val placements = DayGapScheduler.placeTasksInGaps(durations, busy, cursor, bedMinutes)
            placements.forEach { (id, start) ->
                taskRepository.scheduleTask(
                    id,
                    TaskEntity.PLACEMENT_SCHEDULED,
                    date,
                    ClockFormat.minutesToLocalTime(start),
                )
            }
            val unplaced = durations.map { it.first }.filter { it !in placements.keys }
            return ScheduleDayResult(placements.size, unplaced)
        }

        suspend fun scheduledOpenTasks(date: LocalDate): List<TaskEntity> =
            taskRepository.observeTasksForDate(date).first().filter { task ->
                task.placement == TaskEntity.PLACEMENT_SCHEDULED &&
                    task.startLocalTime != null &&
                    task.status != TaskEntity.STATUS_DONE
            }

        private fun tasksWithStartMinutes(tasks: List<TaskEntity>): List<Pair<TaskEntity, Int>> =
            tasks.mapNotNull { task ->
                val raw = task.startLocalTime ?: return@mapNotNull null
                val start = ClockFormat.parseMinutes(raw) ?: return@mapNotNull null
                task to start
            }

        @Suppress("MaxLineLength")
        private fun firstOverlappingPair(withStarts: List<Pair<TaskEntity, Int>>): Pair<Pair<TaskEntity, Int>, Pair<TaskEntity, Int>>? {
            val sorted = withStarts.sortedBy { it.second }
            for (i in sorted.indices) {
                val (taskA, startA) = sorted[i]
                val endA = startA + taskA.durationMin
                for (j in i + 1 until sorted.size) {
                    val (taskB, startB) = sorted[j]
                    val endB = startB + taskB.durationMin
                    if (startA < endB && startB < endA) {
                        return sorted[i] to sorted[j]
                    }
                }
            }
            return null
        }

        private fun findNextFreeSlot(
            withStarts: List<Pair<TaskEntity, Int>>,
            earliest: Int,
            duration: Int,
            excludeTaskId: String?,
            bedMinutes: Int?,
        ): Int? {
            val busy =
                DayGapScheduler.busyFromScheduled(
                    withStarts
                        .filter { (task, _) -> task.id != excludeTaskId }
                        .map { (task, start) -> Triple(task.id, start, task.durationMin) },
                )
            val bed = bedMinutes ?: (24 * 60)
            return DayGapScheduler.findSlotPublic(earliest, duration, busy, bed)
        }
    }
