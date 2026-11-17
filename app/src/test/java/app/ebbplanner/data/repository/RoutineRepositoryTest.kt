package app.ebbplanner.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.ebbplanner.data.local.EbbDatabase
import app.ebbplanner.data.local.entity.RoutineEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class RoutineRepositoryTest {
    private lateinit var database: EbbDatabase
    private lateinit var repository: RoutineRepository

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        database =
            Room.inMemoryDatabaseBuilder(context, EbbDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        repository = RoutineRepository(database.routineDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun createDefault_observableInFlow() =
        runTest {
            repository.createDefault(title = "Stretch", repeatRule = RoutineEntity.REPEAT_DAILY)
            val list = repository.observeRoutines().first()
            assertEquals(1, list.size)
            assertEquals("Stretch", list.first().title)
            assertEquals(RoutineEntity.REPEAT_DAILY, list.first().repeatRule)
        }
}
