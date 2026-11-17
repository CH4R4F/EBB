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
        suspend fun analyzeOverlap(date: LocalDate): OverlapSituation? {
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
            val overlapMin = earlierEnd - laterStart
            val pushStart = earlierEnd
            val shortenDuration = laterStart - earlierStart
            val freeSlot = findNextFreeSlot(withStarts, earlierEnd, laterTask.durationMin)
            val options =
                listOf(
                    OverlapOption(
                        OverlapResolutionType.PUSH_LATER,
                        "Push later",
                        "${laterTask.title} starts ${ClockFormat.minutesToClock(pushStart)}",
                    ),
                    OverlapOption(
                        OverlapResolutionType.SHORTEN_EARLIER,
                        "Shorten earlier",
                        "${earlierTask.title} becomes $shortenDuration min",
                    ),
                    OverlapOption(
                        OverlapResolutionType.NEXT_FREE_SLOT,
                        "Next free slot",
                        freeSlot?.let { "Move ${laterTask.title} to ${ClockFormat.minutesToClock(it)}" }
                            ?: "No slot found",
                    ),
                    OverlapOption(
                        OverlapResolutionType.KEEP_BOTH,
                        "Keep both",
                        "Fine — I'll juggle",
                    ),
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
                    val newStart = earlier.second + earlier.first.durationMin
                    taskRepository.scheduleTask(
                        later.first.id,
                        TaskEntity.PLACEMENT_SCHEDULED,
                        date,
                        ClockFormat.minutesToLocalTime(newStart),
                    )
                }
                OverlapResolutionType.SHORTEN_EARLIER -> {
                    val newDuration = (later.second - earlier.second).coerceAtLeast(5)
                    taskRepository.updateTaskFields(earlier.first.id) {
                        it.copy(durationMin = newDuration)
                    }
                }
                OverlapResolutionType.NEXT_FREE_SLOT -> {
                    val slotStart = earlier.second + earlier.first.durationMin
                    val slot =
                        findNextFreeSlot(withStarts, slotStart, later.first.durationMin)
                    if (slot == null) {
                        ok = false
                    } else {
                        taskRepository.scheduleTask(
                            later.first.id,
                            TaskEntity.PLACEMENT_SCHEDULED,
                            date,
                            ClockFormat.minutesToLocalTime(slot),
                        )
                    }
                }
            }
            return ok
        }

        suspend fun resolveFirstOverlapPushLater(date: LocalDate): Boolean {
            return applyOverlap(date, OverlapResolutionType.PUSH_LATER)
        }

        suspend fun buildMakeItFitPlan(
            date: LocalDate,
            nowMinutes: Int,
            bedMinutes: Int,
        ): MakeItFitPlan {
            val scheduled =
                scheduledOpenTasks(date).filter { task ->
                    val raw = task.startLocalTime ?: return@filter false
                    val start = ClockFormat.parseMinutes(raw) ?: return@filter false
                    !task.pinned && start >= nowMinutes
                }
            val planned = scheduled.sumOf { it.durationMin }
            val available = (bedMinutes - nowMinutes).coerceAtLeast(0)
            val moves = mutableListOf<FitMoveSuggestion>()
            var runningPlanned = planned
            val victims =
                scheduled.sortedByDescending { ClockFormat.parseMinutes(it.startLocalTime ?: "00:00") ?: 0 }
            for (task in victims) {
                if (runningPlanned <= available) break
                moves +=
                    FitMoveSuggestion(
                        taskId = task.id,
                        title = task.title,
                        durationMin = task.durationMin,
                        reason = "Move to Inbox — frees ${task.durationMin} min",
                        selected = true,
                    )
                runningPlanned -= task.durationMin
            }
            return MakeItFitPlan(
                plannedMinutes = planned,
                availableMinutes = available,
                afterPlannedMinutes = runningPlanned.coerceAtMost(available),
                moves = moves,
            )
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
        ): Int {
            if (taskIds.isEmpty()) return 0
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
            return placements.size
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
        ): Int? {
            val busy =
                DayGapScheduler.busyFromScheduled(
                    withStarts.map { (task, start) -> Triple(task.id, start, task.durationMin) },
                )
            return DayGapScheduler.findSlotPublic(earliest, duration, busy, 24 * 60)
        }
    }
