package app.ebbplanner.data.preferences

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import app.ebbplanner.data.preferences.PendingRoutineLink
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class RoutineDeferredWorkPreferencesTest {
    private lateinit var preferences: RoutineDeferredWorkPreferences

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        preferences = RoutineDeferredWorkPreferences(context)
    }

    @Test
    fun removePendingLink_leavesRemainingEntriesInQueue() =
        runTest {
            preferences.enqueuePendingLink("r1", "t1")
            preferences.enqueuePendingLink("r2", "t2")

            preferences.removePendingLink("r1", "t1")

            val remaining = preferences.snapshotPendingLinks()
            assertEquals(1, remaining.size)
            assertEquals(PendingRoutineLink("r2", "t2", 0), remaining.single())
        }

    @Test
    fun snapshotPendingLinks_doesNotClearQueue() =
        runTest {
            preferences.enqueuePendingSync("routine-a")

            val first = preferences.snapshotPendingSyncRoutineIds()
            val second = preferences.snapshotPendingSyncRoutineIds()

            assertEquals(listOf("routine-a"), first)
            assertEquals(first, second)
        }

    @Test
    fun removePendingSync_onlyDropsRequestedId() =
        runTest {
            preferences.enqueuePendingSync("a")
            preferences.enqueuePendingSync("b")

            preferences.removePendingSync("a")

            val remaining = preferences.snapshotPendingSyncRoutineIds()
            assertEquals(listOf("b"), remaining)
            assertTrue(remaining.isNotEmpty())
        }
}