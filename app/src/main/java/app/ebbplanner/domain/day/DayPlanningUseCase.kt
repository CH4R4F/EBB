package app.ebbplanner.domain.day

import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.repository.TaskRepository
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
    ) {
        suspend fun resolveFirstOverlapPushLater(date: LocalDate): Boolean {
            val scheduled = scheduledOpenTasks(date)
            val withStarts = tasksWithStartMinutes(scheduled)
            val overlap = findFirstOverlap(withStarts) ?: return false
            val later = overlap.later
            taskRepository.scheduleTask(
                later.id,
                TaskEntity.PLACEMENT_SCHEDULED,
                date,
                ClockFormat.minutesToLocalTime(overlap.newStartMinutes),
            )
            return true
        }

        private suspend fun scheduledOpenTasks(date: LocalDate): List<TaskEntity> =
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

        private data class OverlapResolution(
            val later: TaskEntity,
            val newStartMinutes: Int,
        )

        private fun findFirstOverlap(withStarts: List<Pair<TaskEntity, Int>>): OverlapResolution? {
            for (i in withStarts.indices) {
                val hit = overlapAtIndex(withStarts, i) ?: continue
                return hit
            }
            return null
        }

        private fun overlapAtIndex(
            withStarts: List<Pair<TaskEntity, Int>>,
            i: Int,
        ): OverlapResolution? {
            val (taskA, startA) = withStarts[i]
            val endA = startA + taskA.durationMin
            for (j in i + 1 until withStarts.size) {
                val (taskB, startB) = withStarts[j]
                val endB = startB + taskB.durationMin
                if (startA < endB && startB < endA) {
                    val later = if (startA <= startB) taskB else taskA
                    val earlierEnd = if (later.id == taskB.id) endA else endB
                    return OverlapResolution(later, earlierEnd)
                }
            }
            return null
        }

        suspend fun makeDayFit(
            date: LocalDate,
            availableMinutes: Int,
        ): Int {
            val scheduled = scheduledOpenTasks(date)
            var planned = scheduled.sumOf { it.durationMin }
            if (planned <= availableMinutes) return 0
            val victims =
                scheduled
                    .sortedByDescending { ClockFormat.parseMinutes(it.startLocalTime ?: "00:00") ?: 0 }
            var moved = 0
            for (task in victims) {
                if (planned <= availableMinutes) break
                taskRepository.scheduleTask(
                    task.id,
                    TaskEntity.PLACEMENT_INBOX,
                    null,
                    null,
                )
                planned -= task.durationMin
                moved++
            }
            return moved
        }

        suspend fun scheduleTasksOnDay(
            date: LocalDate,
            taskIds: List<String>,
            startMinutes: Int,
        ): Int {
            var cursor = startMinutes
            var placed = 0
            for (id in taskIds) {
                val task = taskRepository.getTask(id) ?: continue
                taskRepository.scheduleTask(
                    id,
                    TaskEntity.PLACEMENT_SCHEDULED,
                    date,
                    ClockFormat.minutesToLocalTime(cursor),
                )
                cursor += task.durationMin + 5
                placed++
            }
            return placed
        }
    }
