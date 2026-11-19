package app.ebbplanner.ui.taskdetail

import android.content.Context
import android.provider.Settings
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.ebbplanner.data.local.EbbDatabase
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.repository.TaskRepository
import app.ebbplanner.ui.components.EBB_SHEET_CONFIRM_TAG
import app.ebbplanner.ui.theme.EbbTheme
import app.ebbplanner.ui.time.ClockFormat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [28], qualifiers = "w411dp-h891dp-normal-long-notround-any-420dpi-keyshidden-nonav")
class TaskScheduleSheetTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun useSelection_withoutChanges_keepsExistingDateAndTime() {
        val existingDate = LocalDate.of(2026, 3, 15)
        val existingStart = "14:30"
        var savedPlacement: String? = null
        var savedDate: LocalDate? = null
        var savedStart: String? = null
        composeRule.setContent {
            EbbTheme {
                ScheduleSheetPanel(
                    taskTitle = "Write brief",
                    suggestions = emptyList(),
                    existingDate = existingDate,
                    existingStartLocalTime = existingStart,
                    onSave = { placement, date, time ->
                        savedPlacement = placement
                        savedDate = date
                        savedStart = time
                    },
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("15 Mar").assertIsDisplayed()
        val is24Hour =
            android.text.format.DateFormat.is24HourFormat(
                androidx.test.core.app.ApplicationProvider.getApplicationContext(),
            )
        composeRule
            .onNodeWithText(ClockFormat.minutesToClockDisplay(14 * 60 + 30, is24Hour))
            .assertIsDisplayed()
        composeRule.onNodeWithTag(EBB_SHEET_CONFIRM_TAG).performTouchInput {
            click(Offset(width / 2f, height / 2f))
        }
        composeRule.waitForIdle()
        assertEquals(TaskEntity.PLACEMENT_SCHEDULED, savedPlacement)
        assertEquals(existingDate, savedDate)
        assertEquals(existingStart, savedStart)
    }

    @Test
    fun timeChip_24Hour_zeroPads() {
        setClock24Hour(true)
        composeRule.setContent {
            EbbTheme {
                ScheduleSheetPanel(
                    taskTitle = "Write brief",
                    suggestions = emptyList(),
                    existingDate = LocalDate.of(2026, 3, 15),
                    existingStartLocalTime = "09:00",
                    onSave = { _, _, _ -> },
                )
            }
        }
        composeRule.onNodeWithText("09:00").assertIsDisplayed()
    }

    @Test
    fun timeChip_12Hour_noZeroPad() {
        setClock24Hour(false)
        composeRule.setContent {
            EbbTheme {
                ScheduleSheetPanel(
                    taskTitle = "Write brief",
                    suggestions = emptyList(),
                    existingDate = LocalDate.of(2026, 3, 15),
                    existingStartLocalTime = "09:00",
                    onSave = { _, _, _ -> },
                )
            }
        }
        composeRule.onNodeWithText("9:00 AM").assertIsDisplayed()
        composeRule.onNodeWithText(ClockFormat.minutesToClockDisplay(9 * 60, is24Hour = false)).assertIsDisplayed()
    }

    @Test
    fun suggestionRow_24h_matchesChip() {
        setClock24Hour(true)
        val minutes = 20 * 60 + 5
        val clock = ClockFormat.minutesToClockDisplay(minutes, is24Hour = true)
        composeRule.setContent {
            EbbTheme {
                ScheduleSheetPanel(
                    taskTitle = "Call",
                    suggestions =
                        listOf(
                            ScheduleSuggestion(
                                label = "Today · $clock",
                                subtitle = "Next free slot",
                                placement = TaskEntity.PLACEMENT_SCHEDULED,
                                date = LocalDate.of(2026, 3, 15),
                                startLocalTime = "20:05",
                            ),
                        ),
                    existingDate = LocalDate.of(2026, 3, 15),
                    existingStartLocalTime = "20:05",
                    onSave = { _, _, _ -> },
                )
            }
        }
        composeRule.onNodeWithText("Today · 20:05").assertIsDisplayed()
        composeRule.onNodeWithText("20:05").assertIsDisplayed()
    }

    @Test
    fun suggestionRow_12h_matchesChip() {
        setClock24Hour(false)
        val minutes = 20 * 60 + 5
        val clock = ClockFormat.minutesToClockDisplay(minutes, is24Hour = false)
        composeRule.setContent {
            EbbTheme {
                ScheduleSheetPanel(
                    taskTitle = "Call",
                    suggestions =
                        listOf(
                            ScheduleSuggestion(
                                label = "Today · $clock",
                                subtitle = "Next free slot",
                                placement = TaskEntity.PLACEMENT_SCHEDULED,
                                date = LocalDate.of(2026, 3, 15),
                                startLocalTime = "20:05",
                            ),
                        ),
                    existingDate = LocalDate.of(2026, 3, 15),
                    existingStartLocalTime = "20:05",
                    onSave = { _, _, _ -> },
                )
            }
        }
        composeRule.onNodeWithText("Today · 8:05 PM").assertIsDisplayed()
        composeRule.onNodeWithText("8:05 PM").assertIsDisplayed()
    }

    @Test
    fun useSelection_withoutChanges_keepsReminderEnabledAndAlarm() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val database =
            Room.inMemoryDatabaseBuilder(context, EbbDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        val scheduler = app.ebbplanner.reminders.ReminderScheduler(context, alarmManager)
        val grant =
            mockk<app.ebbplanner.reminders.ExactAlarmGrant> {
                coEvery { isGranted() } returns true
            }
        val sync =
            app.ebbplanner.reminders.TaskReminderSync(
                database.taskDao(),
                database.reminderDao(),
                scheduler,
                grant,
            )
        val prefs = mockk<app.ebbplanner.data.preferences.UserPreferencesRepository>(relaxed = true)
        every { prefs.remindersDefault } returns kotlinx.coroutines.flow.flowOf(false)
        val repository =
            TaskRepository(
                database.taskDao(),
                database.dayPlanDao(),
                database.subtaskDao(),
                sync,
                prefs,
                mockk(relaxed = true),
            )
        val date = LocalDate.now().plusDays(1)
        val task =
            runBlocking {
                repository.addQuickCapture(
                    "Call",
                    date,
                    TaskEntity.PLACEMENT_SCHEDULED,
                    "21:40",
                    reminderEnabled = true,
                )
            }
        val before = runBlocking { database.reminderDao().getAll().single { it.taskId == task.id } }
        composeRule.setContent {
            EbbTheme {
                ScheduleSheetPanel(
                    taskTitle = "Call",
                    suggestions = emptyList(),
                    existingDate = date,
                    existingStartLocalTime = "21:40",
                    onSave = { placement, savedDate, time ->
                        runBlocking { repository.scheduleTask(task.id, placement, savedDate, time) }
                    },
                )
            }
        }
        composeRule.onNodeWithTag(EBB_SHEET_CONFIRM_TAG).performTouchInput {
            click(Offset(width / 2f, height / 2f))
        }
        composeRule.waitForIdle()
        val stored = runBlocking { repository.getTask(task.id) }
        assertEquals(true, stored?.reminderEnabled)
        val after = runBlocking { database.reminderDao().getAll().single { it.taskId == task.id } }
        assertEquals(before.id, after.id)
        assertEquals(before.fireAtEpochMillis, after.fireAtEpochMillis)
        val intent =
            android.content.Intent(context, app.ebbplanner.reminders.ReminderAlarmReceiver::class.java).apply {
                putExtra(app.ebbplanner.reminders.ReminderAlarmReceiver.EXTRA_TASK_ID, task.id)
            }
        val pending =
            android.app.PendingIntent.getBroadcast(
                context,
                app.ebbplanner.reminders.ReminderScheduler.alarmRequestCode(task.id),
                intent,
                android.app.PendingIntent.FLAG_NO_CREATE or android.app.PendingIntent.FLAG_IMMUTABLE,
            )
        assertNotNull(pending)
        database.close()
    }

    private fun setClock24Hour(enabled: Boolean) {
        Settings.System.putString(
            ApplicationProvider.getApplicationContext<Context>().contentResolver,
            Settings.System.TIME_12_24,
            if (enabled) "24" else "12",
        )
    }
}
