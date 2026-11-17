package app.ebbplanner.reminders

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import app.ebbplanner.data.repository.TaskRepository
import app.ebbplanner.domain.reminders.ReminderKind
import app.ebbplanner.domain.reminders.ReminderRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRescheduleUseCase
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val scheduler: ReminderScheduler,
        private val taskRepository: TaskRepository,
    ) {
        fun rescheduleNow() {
            runBlocking {
                val tasks = taskRepository.observeTasksForDate(LocalDate.now()).first()
                val zone = ZoneId.systemDefault()
                val requests =
                    tasks.mapNotNull { task ->
                        val start = task.startLocalTime ?: return@mapNotNull null
                        val parts = start.split(":")
                        val hour = parts.getOrNull(0)?.toIntOrNull() ?: return@mapNotNull null
                        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
                        val fire =
                            LocalDate
                                .parse(task.date ?: LocalDate.now().toString())
                                .atTime(hour, minute)
                                .atZone(zone)
                                .toInstant()
                                .toEpochMilli()
                        ReminderRequest(
                            taskId = task.id,
                            fireAtEpochMillis = fire,
                            kind = ReminderKind.TaskStart,
                            title = task.title,
                            exact = true,
                        )
                    }
                scheduler.rescheduleAll(requests, exactAlarmsGranted())
            }
        }

        fun snoozeTask(taskId: String?) {
            if (taskId == null) return
            val request =
                ReminderRequest(
                    taskId = taskId,
                    fireAtEpochMillis = System.currentTimeMillis() + SNOOZE_MILLIS,
                    kind = ReminderKind.TaskStart,
                    title = "Snoozed",
                    exact = true,
                )
            scheduler.rescheduleAll(listOf(request), exactAlarmsGranted())
        }

        private fun exactAlarmsGranted(): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            return alarmManager.canScheduleExactAlarms()
        }

        companion object {
            private const val SNOOZE_MILLIS = 10 * 60 * 1000L
        }
    }
