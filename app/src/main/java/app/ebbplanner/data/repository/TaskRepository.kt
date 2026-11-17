package app.ebbplanner.data.repository

import app.ebbplanner.data.local.dao.DayPlanDao
import app.ebbplanner.data.local.dao.TaskDao
import app.ebbplanner.data.local.entity.DayPlanEntity
import app.ebbplanner.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository
    @Inject
    constructor(
        private val taskDao: TaskDao,
        private val dayPlanDao: DayPlanDao,
    ) {
        private val zoneId: ZoneId = ZoneId.systemDefault()

        fun observeTasksForDate(date: LocalDate): Flow<List<TaskEntity>> = taskDao.observeForDate(date.toString())

        fun observeDayPlan(date: LocalDate): Flow<DayPlanEntity?> = dayPlanDao.observeForDate(date.toString())

        suspend fun ensureSampleDataForTodayIfEmpty(date: LocalDate) {
            if (taskDao.countActive() > 0) return
            val now = System.currentTimeMillis()
            val dateString = date.toString()
            taskDao.insertAll(buildSampleTasks(dateString, now))
            dayPlanDao.insert(
                DayPlanEntity(
                    id = UUID.randomUUID().toString(),
                    date = dateString,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        }

        private fun buildSampleTasks(
            dateString: String,
            now: Long,
        ): List<TaskEntity> =
            listOf(
                sampleTask(
                    title = "Morning check-in",
                    placement = TaskEntity.PLACEMENT_SCHEDULED,
                    dateString = dateString,
                    startLocalTime = "09:00",
                    durationMin = 30,
                    colorKey = "green",
                    now = now,
                ),
                sampleTask(
                    title = "Deep work",
                    placement = TaskEntity.PLACEMENT_SCHEDULED,
                    dateString = dateString,
                    startLocalTime = "10:30",
                    durationMin = 90,
                    colorKey = "sky",
                    now = now,
                ),
                sampleTask(
                    title = "Drink water",
                    placement = TaskEntity.PLACEMENT_ANYTIME,
                    dateString = dateString,
                    durationMin = 5,
                    colorKey = "lemon",
                    now = now,
                ),
                sampleTask(
                    title = "Buy stamps",
                    placement = TaskEntity.PLACEMENT_ANYTIME,
                    dateString = dateString,
                    durationMin = 15,
                    colorKey = "rose",
                    now = now,
                ),
            )

        private fun sampleTask(
            title: String,
            placement: String,
            dateString: String,
            now: Long,
            startLocalTime: String? = null,
            durationMin: Int = TaskEntity.DEFAULT_DURATION_MIN,
            colorKey: String = "slate",
        ): TaskEntity =
            TaskEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                placement = placement,
                date = dateString,
                startLocalTime = startLocalTime,
                zoneId = zoneId.id,
                durationMin = durationMin,
                colorKey = colorKey,
                createdAt = now,
                updatedAt = now,
            )

        suspend fun insertTask(task: TaskEntity) {
            taskDao.insert(task)
        }

        suspend fun updateTask(task: TaskEntity) {
            taskDao.update(task)
        }

        suspend fun deleteTask(id: String) {
            val now = System.currentTimeMillis()
            taskDao.softDelete(id, now, now)
        }

        suspend fun getTask(id: String): TaskEntity? = taskDao.getById(id)

        suspend fun addQuickCapture(
            title: String,
            date: LocalDate,
            placement: String = TaskEntity.PLACEMENT_INBOX,
            startLocalTime: String? = null,
            durationMin: Int = TaskEntity.DEFAULT_DURATION_MIN,
        ): TaskEntity {
            val now = System.currentTimeMillis()
            val task =
                TaskEntity(
                    id = UUID.randomUUID().toString(),
                    title = title.trim(),
                    placement = placement,
                    date = if (placement == TaskEntity.PLACEMENT_INBOX) null else date.toString(),
                    startLocalTime = startLocalTime,
                    zoneId = zoneId.id,
                    durationMin = durationMin,
                    createdAt = now,
                    updatedAt = now,
                )
            taskDao.insert(task)
            return task
        }
    }
