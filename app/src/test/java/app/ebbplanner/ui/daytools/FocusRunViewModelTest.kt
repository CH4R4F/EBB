package app.ebbplanner.ui.daytools

import androidx.lifecycle.SavedStateHandle
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.repository.TaskRepository
import app.ebbplanner.domain.time.MutableEbbClock
import app.ebbplanner.reminders.FocusNotifier
import app.ebbplanner.ui.focus.FocusVisualMode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class FocusRunViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val clock = MutableEbbClock(Instant.parse("2026-11-17T10:00:00Z"))
    private val taskRepo = mockk<TaskRepository>(relaxed = true)
    private val notifier = mockk<FocusNotifier>(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadsOpenedTaskTitle_notHeuristic() =
        runTest {
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
                )
            advanceUntilIdle()
            assertEquals("Opened task", vm.state.value.title)
        }

    @Test
    fun justFiveMinutes_entersRunningMode() =
        runTest {
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
                )
            advanceUntilIdle()
            vm.justFiveMinutes()
            advanceUntilIdle()
            assertEquals(FocusVisualMode.RunningRing, vm.state.value.mode)
        }

    @Test
    fun markDone_persistsTask() =
        runTest {
            val id = "task-3"
            coEvery { taskRepo.getTask(id) } returns
                TaskEntity(id = id, title = "T", createdAt = 0L, updatedAt = 0L)
            val vm =
                FocusRunViewModel(
                    SavedStateHandle(mapOf("taskId" to id)),
                    taskRepo,
                    clock,
                    notifier,
                )
            advanceUntilIdle()
            vm.markDone()
            advanceUntilIdle()
            coVerify { taskRepo.markTaskDone(id) }
        }
}
