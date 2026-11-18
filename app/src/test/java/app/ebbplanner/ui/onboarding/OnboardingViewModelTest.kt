package app.ebbplanner.ui.onboarding

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.ebbplanner.MainDispatcherRule
import app.ebbplanner.data.local.EbbDatabase
import app.ebbplanner.data.preferences.UserPreferencesRepository
import app.ebbplanner.data.repository.TaskRepository
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class OnboardingViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var preferencesRepository: UserPreferencesRepository
    private lateinit var taskRepository: TaskRepository
    private lateinit var database: EbbDatabase

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        preferencesRepository = UserPreferencesRepository(context)
        database =
            Room.inMemoryDatabaseBuilder(context, EbbDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        taskRepository =
            TaskRepository(
                database.taskDao(),
                database.dayPlanDao(),
                database.subtaskDao(),
                mockk(relaxed = true),
                preferencesRepository,
                mockk(relaxed = true),
            )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun completeOnboarding_persistsPreferences() =
        runTest {
            val viewModel = OnboardingViewModel(preferencesRepository, taskRepository)
            viewModel.setWakeMinutes(8 * 60)
            viewModel.setBedMinutes(22 * 60)
            viewModel.setAddSampleTasks(true)
            viewModel.persistOnboardingChoices()

            assertTrue(preferencesRepository.onboardingComplete.first())
            assertEquals(8 * 60, preferencesRepository.dayWakeMinutes.first())
            assertEquals(22 * 60, preferencesRepository.dayBedMinutes.first())
            assertTrue(preferencesRepository.seedSampleTasks.first())
        }
}
