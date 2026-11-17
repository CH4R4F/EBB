package app.ebbplanner.domain.day

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.ebbplanner.data.local.EbbDatabase
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.preferences.UserPreferencesRepository
import app.ebbplanner.data.repository.TaskRepository
import app.ebbplanner.domain.time.MutableEbbClock
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class DayPlanningUseCaseTest {
    private lateinit var database: EbbDatabase
    private lateinit var repository: TaskRepository
    private lateinit var useCase: DayPlanningUseCase

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        database =
            Room.inMemoryDatabaseBuilder(context, EbbDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        val prefs = mockk<UserPreferencesRepository>(relaxed = true)
        every { prefs.remindersDefault } returns flowOf(true)
        repository =
            TaskRepository(
                database.taskDao(),
                database.dayPlanDao(),
                database.subtaskDao(),
                mockk(relaxed = true),
                prefs,
                mockk(relaxed = true),
            )
        val clock = MutableEbbClock(Instant.parse("2026-11-17T10:00:00Z"))
        useCase = DayPlanningUseCase(repository, clock)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun resolveOverlap_pushLater_separatesTasks() =
        runTest {
            val date = LocalDate.now()
            val now = System.currentTimeMillis()
            val a = UUID.randomUUID().toString()
            val b = UUID.randomUUID().toString()
            database.taskDao().insertAll(
                listOf(
                    TaskEntity(
                        id = a,
                        title = "A",
                        placement = TaskEntity.PLACEMENT_SCHEDULED,
                        date = date.toString(),
                        startLocalTime = "10:00",
                        durationMin = 60,
                        createdAt = now,
                        updatedAt = now,
                    ),
                    TaskEntity(
                        id = b,
                        title = "B",
                        placement = TaskEntity.PLACEMENT_SCHEDULED,
                        date = date.toString(),
                        startLocalTime = "10:30",
                        durationMin = 60,
                        createdAt = now,
                        updatedAt = now,
                    ),
                ),
            )
            assertTrue(useCase.resolveFirstOverlapPushLater(date))
            assertEquals("11:00", repository.getTask(b)?.startLocalTime)
        }

    @Test
    fun analyzeOverlap_reportsRealTitles() =
        runTest {
            val date = LocalDate.of(2026, 11, 17)
            val now = System.currentTimeMillis()
            database.taskDao().insertAll(
                listOf(
                    TaskEntity(
                        id = "a",
                        title = "Alpha",
                        placement = TaskEntity.PLACEMENT_SCHEDULED,
                        date = date.toString(),
                        startLocalTime = "10:00",
                        durationMin = 60,
                        createdAt = now,
                        updatedAt = now,
                    ),
                    TaskEntity(
                        id = "b",
                        title = "Beta",
                        placement = TaskEntity.PLACEMENT_SCHEDULED,
                        date = date.toString(),
                        startLocalTime = "10:30",
                        durationMin = 30,
                        createdAt = now,
                        updatedAt = now,
                    ),
                ),
            )
            val situation = useCase.analyzeOverlap(date)
            requireNotNull(situation)
            assertEquals("Alpha", situation.firstTitle)
            assertEquals("Beta", situation.secondTitle)
            assertTrue(useCase.applyOverlap(date, OverlapResolutionType.SHORTEN_EARLIER))
        }

    @Test
    fun planPlacesInGapWithoutOverlap() =
        runTest {
            val date = LocalDate.of(2026, 11, 17)
            val now = System.currentTimeMillis()
            val scheduled =
                TaskEntity(
                    id = "busy",
                    title = "Busy",
                    placement = TaskEntity.PLACEMENT_SCHEDULED,
                    date = date.toString(),
                    startLocalTime = "10:00",
                    durationMin = 60,
                    createdAt = now,
                    updatedAt = now,
                )
            val inbox =
                TaskEntity(
                    id = "inbox1",
                    title = "Place me",
                    placement = TaskEntity.PLACEMENT_INBOX,
                    durationMin = 15,
                    createdAt = now,
                    updatedAt = now,
                )
            database.taskDao().insertAll(listOf(scheduled, inbox))
            val placed = useCase.scheduleTasksOnDay(date, listOf("inbox1"), 9 * 60, 23 * 60, 10 * 60 + 5)
            assertEquals(1, placed)
            val start = repository.getTask("inbox1")?.startLocalTime
            assertEquals("11:00", start)
        }

    @Test
    fun makeItFit_movesSelectedToInbox() =
        runTest {
            val date = LocalDate.of(2026, 11, 17)
            val now = System.currentTimeMillis()
            database.taskDao().insert(
                TaskEntity(
                    id = "late",
                    title = "Late",
                    placement = TaskEntity.PLACEMENT_SCHEDULED,
                    date = date.toString(),
                    startLocalTime = "15:00",
                    durationMin = 120,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
            val plan = useCase.buildMakeItFitPlan(date, 14 * 60, 15 * 60 + 30)
            assertTrue(plan.moves.isNotEmpty())
            val moved = useCase.applyMakeItFit(date, 14 * 60, 15 * 60 + 30, setOf("late"))
            assertEquals(1, moved)
            assertEquals(TaskEntity.PLACEMENT_INBOX, repository.getTask("late")?.placement)
        }
}
