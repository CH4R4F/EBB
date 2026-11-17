package app.ebbplanner.domain.day

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.ebbplanner.data.local.EbbDatabase
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.repository.TaskRepository
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
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
        repository =
            TaskRepository(
                database.taskDao(),
                database.dayPlanDao(),
                database.subtaskDao(),
                mockk(relaxed = true),
            )
        useCase = DayPlanningUseCase(repository)
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
}
