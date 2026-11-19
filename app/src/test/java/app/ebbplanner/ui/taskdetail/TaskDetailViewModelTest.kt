package app.ebbplanner.ui.taskdetail

import android.content.Context
import android.os.Looper
import android.provider.Settings
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.ebbplanner.MainDispatcherRule
import app.ebbplanner.data.local.EbbDatabase
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.repository.TaskRepository
import app.ebbplanner.domain.day.DayPlanningUseCase
import app.ebbplanner.domain.time.EbbClock
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class TaskDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var database: EbbDatabase
    private lateinit var repository: TaskRepository
    private lateinit var planningUseCase: DayPlanningUseCase
    private lateinit var prefs: app.ebbplanner.data.preferences.UserPreferencesRepository
    private lateinit var clock: EbbClock

    private fun createViewModel(): TaskDetailViewModel =
        TaskDetailViewModel(
            ApplicationProvider.getApplicationContext(),
            repository,
            prefs,
            planningUseCase,
            clock,
        )

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        database =
            Room.inMemoryDatabaseBuilder(context, EbbDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        prefs = mockk(relaxed = true)
        every { prefs.remindersDefault } returns kotlinx.coroutines.flow.flowOf(true)
        every { prefs.breatherMinutes } returns kotlinx.coroutines.flow.flowOf(5)
        every { prefs.dayBedMinutes } returns kotlinx.coroutines.flow.flowOf(23 * 60)
        every { prefs.dayWakeMinutes } returns kotlinx.coroutines.flow.flowOf(7 * 60)
        clock =
            mockk<EbbClock>().also {
                every { it.today() } returns LocalDate.now()
                every { it.nowMinutes() } returns 10 * 60
                every { it.zoneId() } returns ZoneId.systemDefault()
            }
        repository =
            TaskRepository(
                database.taskDao(),
                database.dayPlanDao(),
                database.subtaskDao(),
                mockk(relaxed = true),
                prefs,
                mockk(relaxed = true),
            )
        planningUseCase = DayPlanningUseCase(repository, clock)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun openSheetFromRoute_opensSchedule() {
        val vm = createViewModel()
        vm.openSheetFromRoute("schedule")
        assertEquals(TaskSheetType.Schedule, vm.openSheet.value)
    }

    @Test
    fun load_unknownId_eventuallyNotFixtureTitle() =
        runBlocking {
            val vm = createViewModel()
            vm.load("missing-id")
            assertNotEquals("Write project brief", vm.uiState.value.title)
            assertTrue(
                vm.uiState.value.mode == TaskDetailMode.Loading ||
                    vm.uiState.value.mode == TaskDetailMode.NotFound,
            )
        }

    @Test
    fun load_existingTask_doesNotUseFixtureTitle() =
        runBlocking {
            val task =
                repository.addQuickCapture(
                    "Database title",
                    LocalDate.now(),
                    TaskEntity.PLACEMENT_ANYTIME,
                )
            val vm = createViewModel()
            vm.load(task.id)
            kotlinx.coroutines.delay(100)
            val title = vm.uiState.value.title
            if (title.isNotEmpty()) {
                assertEquals("Database title", title)
            }
        }

    @Test
    fun scheduleSuggestions_24h_zeroPadsNextFree() =
        runBlocking {
            setClock24Hour(true)
            every { clock.nowMinutes() } returns 20 * 60 + 5
            val task =
                repository.addQuickCapture(
                    "Call",
                    LocalDate.now(),
                    TaskEntity.PLACEMENT_INBOX,
                )
            val vm = createViewModel()
            vm.load(task.id)
            waitUntil { vm.uiState.value.scheduleSuggestions.any { it.label.startsWith("Today ·") } }
            assertEquals(
                "Today · 20:05",
                vm.uiState.value.scheduleSuggestions.single { it.subtitle == "Next free slot" }.label,
            )
        }

    @Test
    fun scheduleSuggestions_12h_usesUnpaddedClock() =
        runBlocking {
            setClock24Hour(false)
            every { clock.nowMinutes() } returns 20 * 60 + 5
            val task =
                repository.addQuickCapture(
                    "Call",
                    LocalDate.now(),
                    TaskEntity.PLACEMENT_INBOX,
                )
            val vm = createViewModel()
            vm.load(task.id)
            waitUntil { vm.uiState.value.scheduleSuggestions.any { it.label.startsWith("Today ·") } }
            assertEquals(
                "Today · 8:05 PM",
                vm.uiState.value.scheduleSuggestions.single { it.subtitle == "Next free slot" }.label,
            )
        }

    @Test
    fun saveSchedule_sameDateTime_keepsReminderEnabledAndRow() =
        runBlocking {
            every { prefs.remindersDefault } returns kotlinx.coroutines.flow.flowOf(false)
            val (repo, _) = reminderAwareRepo(defaultReminders = false)
            repository = repo
            planningUseCase = DayPlanningUseCase(repository, clock)
            val date = LocalDate.now().plusDays(1)
            val task =
                repository.addQuickCapture(
                    "Call",
                    date,
                    TaskEntity.PLACEMENT_SCHEDULED,
                    "21:40",
                    reminderEnabled = true,
                )
            val before = database.reminderDao().getAll().single { it.taskId == task.id }
            val vm = createViewModel()
            vm.load(task.id)
            waitUntil { vm.uiState.value.title == "Call" }
            vm.saveSchedule(TaskEntity.PLACEMENT_SCHEDULED, date, "21:40")
            waitUntil { repository.getTask(task.id)?.startLocalTime == "21:40" }
            assertEquals(true, repository.getTask(task.id)?.reminderEnabled)
            val after = database.reminderDao().getAll().single { it.taskId == task.id }
            assertEquals(before.id, after.id)
            assertEquals(before.fireAtEpochMillis, after.fireAtEpochMillis)
        }

    @Test
    fun saveSchedule_timeChange_reschedulesReminder() =
        runBlocking {
            every { prefs.remindersDefault } returns kotlinx.coroutines.flow.flowOf(false)
            val (repo, _) = reminderAwareRepo(defaultReminders = false)
            repository = repo
            planningUseCase = DayPlanningUseCase(repository, clock)
            val date = LocalDate.now().plusDays(1)
            val task =
                repository.addQuickCapture(
                    "Call",
                    date,
                    TaskEntity.PLACEMENT_SCHEDULED,
                    "21:40",
                    reminderEnabled = true,
                )
            val before = database.reminderDao().getAll().single { it.taskId == task.id }
            val vm = createViewModel()
            vm.load(task.id)
            waitUntil { vm.uiState.value.title == "Call" }
            vm.saveSchedule(TaskEntity.PLACEMENT_SCHEDULED, date, "22:10")
            waitUntil {
                val after = database.reminderDao().getAll().singleOrNull { it.taskId == task.id }
                repository.getTask(task.id)?.startLocalTime == "22:10" &&
                    after != null &&
                    after.fireAtEpochMillis > before.fireAtEpochMillis
            }
            assertEquals(true, repository.getTask(task.id)?.reminderEnabled)
            val after = database.reminderDao().getAll().single { it.taskId == task.id }
            assertTrue(after.fireAtEpochMillis > before.fireAtEpochMillis)
        }

    private fun setClock24Hour(enabled: Boolean) {
        Settings.System.putString(
            ApplicationProvider.getApplicationContext<Context>().contentResolver,
            Settings.System.TIME_12_24,
            if (enabled) "24" else "12",
        )
    }

    private suspend fun waitUntil(
        timeoutMs: Long = 5_000,
        condition: suspend () -> Boolean,
    ) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            shadowOf(Looper.getMainLooper()).idle()
            if (condition()) return
            kotlinx.coroutines.delay(25)
        }
        shadowOf(Looper.getMainLooper()).idle()
        assertTrue(condition())
    }

    private fun reminderAwareRepo(defaultReminders: Boolean): Pair<TaskRepository, app.ebbplanner.reminders.ReminderScheduler> {
        val context: Context = ApplicationProvider.getApplicationContext()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        val scheduler = app.ebbplanner.reminders.ReminderScheduler(context, alarmManager)
        val grant =
            io.mockk.mockk<app.ebbplanner.reminders.ExactAlarmGrant> {
                io.mockk.coEvery { isGranted() } returns true
            }
        val sync =
            app.ebbplanner.reminders.TaskReminderSync(
                database.taskDao(),
                database.reminderDao(),
                scheduler,
                grant,
            )
        every { prefs.remindersDefault } returns kotlinx.coroutines.flow.flowOf(defaultReminders)
        val repo =
            TaskRepository(
                database.taskDao(),
                database.dayPlanDao(),
                database.subtaskDao(),
                sync,
                prefs,
                io.mockk.mockk(relaxed = true),
            )
        return repo to scheduler
    }
}
