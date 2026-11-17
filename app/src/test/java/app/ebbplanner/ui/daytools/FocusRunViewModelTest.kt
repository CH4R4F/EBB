package app.ebbplanner.ui.daytools

import androidx.lifecycle.SavedStateHandle
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.repository.TaskRepository
import app.ebbplanner.domain.focus.FocusTickDelayer
import app.ebbplanner.domain.time.MutableEbbClock
import app.ebbplanner.reminders.FocusNotifier
import app.ebbplanner.ui.focus.FocusVisualMode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class FocusRunViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val clock = MutableEbbClock(Instant.parse("2026-11-17T10:00:00Z"))
    private val taskRepo = mockk<TaskRepository>(relaxed = true)
    private val notifier = mockk<FocusNotifier>(relaxed = true)
    private val tickDelayer = FocusTickDelayer { ms -> dispatcher.scheduler.advanceTimeBy(ms) }

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun runVmTest(block: suspend kotlinx.coroutines.test.TestScope.() -> Unit) =
        runTest(dispatcher, timeout = 10.seconds) {
            clock.setMillisProvider { testScheduler.currentTime }
            try {
                block()
            } finally {
                clock.clearMillisProvider()
            }
        }

    @Test
    fun loadsOpenedTaskTitle_notHeuristic() =
        runVmTest {
            val id = "task-1"
            coEvery { taskRepo.getTask(id) } returns
                TaskEntity(
                    id = id,
                    title = "Opened task",
                    placement = TaskEntity.PLACEMENT_SCHEDULED,
                    date = LocalDate.now().toString(),
                    startLocalTime = "10:30",
                    durationMin = 30,
                    createdAt = 0L,
                    updatedAt = 0L,
                )
            val vm =
                FocusRunViewModel(
                    SavedStateHandle(mapOf("taskId" to id)),
                    taskRepo,
                    clock,
                    notifier,
                    tickDelayer,
                )
            runCurrent()
            assertEquals("Opened task", vm.state.value.title)
            vm.close()
        }

    @Test
    fun justFiveMinutes_entersRunningMode() =
        runVmTest {
            val id = "task-2"
            coEvery { taskRepo.getTask(id) } returns
                TaskEntity(
                    id = id,
                    title = "T",
                    createdAt = 0L,
                    updatedAt = 0L,
                )
            val vm =
                FocusRunViewModel(
                    SavedStateHandle(mapOf("taskId" to id)),
                    taskRepo,
                    clock,
                    notifier,
                    tickDelayer,
                )
            runCurrent()
            vm.justFiveMinutes()
            assertEquals(FocusVisualMode.RunningRing, vm.state.value.mode)
            advanceTimeBy(5 * 60 * 1_000L)
            runCurrent()
            assertEquals(FocusVisualMode.TimesUp, vm.state.value.mode)
            vm.close()
        }

    @Test
    fun markDone_persistsTask() =
        runVmTest {
            val id = "task-3"
            coEvery { taskRepo.getTask(id) } returns
                TaskEntity(id = id, title = "T", createdAt = 0L, updatedAt = 0L)
            val vm =
                FocusRunViewModel(
                    SavedStateHandle(mapOf("taskId" to id)),
                    taskRepo,
                    clock,
                    notifier,
                    tickDelayer,
                )
            runCurrent()
            vm.markDone()
            runCurrent()
            coVerify { taskRepo.markTaskDone(id) }
            vm.close()
        }
}
