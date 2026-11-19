package app.ebbplanner.domain.capture

import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.ui.today.QuickCaptureSaveTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class QuickCaptureRoutingTest {
    private val today = LocalDate.of(2026, 11, 19)

    @Test
    fun today_noTime_routesToAnytime() {
        val resolved = resolveQuickCapture(QuickCaptureDraft("gym"), today, QuickCaptureSaveTarget.Today)
        assertEquals(TaskEntity.PLACEMENT_ANYTIME, resolved.placement)
        assertEquals(QuickCaptureDestinationKind.Anytime, resolved.kind)
        assertNull(resolved.startTime)
    }

    @Test
    fun today_parsedTime_routesToTimeline() {
        val resolved = resolveQuickCapture(QuickCaptureDraft("gym 18:00"), today, QuickCaptureSaveTarget.Today)
        assertEquals(TaskEntity.PLACEMENT_SCHEDULED, resolved.placement)
        assertEquals("18:00", resolved.startLocalTime)
        assertEquals(QuickCaptureDestinationKind.Timeline, resolved.kind)
    }

    @Test
    fun today_overrideClearsParsedTime() {
        val resolved =
            resolveQuickCapture(
                QuickCaptureDraft("gym 18:00", startOverridden = true, startOverride = null),
                today,
                QuickCaptureSaveTarget.Today,
            )
        assertEquals(TaskEntity.PLACEMENT_ANYTIME, resolved.placement)
        assertNull(resolved.startTime)
    }

    @Test
    fun today_overrideReplacesParsedTime() {
        val resolved =
            resolveQuickCapture(
                QuickCaptureDraft(
                    "gym 18:00",
                    startOverride = LocalTime.of(9, 30),
                    startOverridden = true,
                ),
                today,
                QuickCaptureSaveTarget.Today,
            )
        assertEquals("09:30", resolved.startLocalTime)
    }

    @Test
    fun inbox_noTime_routesToInbox() {
        val resolved = resolveQuickCapture(QuickCaptureDraft("buy milk"), today, QuickCaptureSaveTarget.Inbox)
        assertEquals(TaskEntity.PLACEMENT_INBOX, resolved.placement)
        assertEquals(QuickCaptureDestinationKind.Inbox, resolved.kind)
    }

    @Test
    fun inbox_tomorrowWithoutTime_routesToAnytime() {
        val resolved = resolveQuickCapture(QuickCaptureDraft("buy milk tomorrow"), today, QuickCaptureSaveTarget.Inbox)
        assertEquals(TaskEntity.PLACEMENT_ANYTIME, resolved.placement)
    }

    @Test
    fun durationOverride_beatsParsed() {
        val resolved =
            resolveQuickCapture(
                QuickCaptureDraft("gym 18:00 45m", durationOverride = 90, durationOverridden = true),
                today,
                QuickCaptureSaveTarget.Today,
            )
        assertEquals(90, resolved.durationMin)
        assertTrue(resolved.explicitDuration)
    }

    @Test
    fun saveDisabledWhenBlank() {
        val resolved = resolveQuickCapture(QuickCaptureDraft("   "), today, QuickCaptureSaveTarget.Today)
        assertFalse(resolved.saveEnabled)
    }

    @Test
    fun nextFreeSlot_skipsOccupiedBlock() {
        val slot = nextFreeSlotMinutes(10 * 60, listOf(10 * 60 to 45), durationMin = 15)
        assertEquals(10 * 60 + 45, slot)
    }

    @Test
    fun nextFreeSlot_respectsSelectedDuration() {
        val occupied =
            listOf(
                10 * 60 to 60,
                11 * 60 + 15 to 30,
                12 * 60 + 20 to 30,
            )
        val slot = nextFreeSlotMinutes(10 * 60, occupied, durationMin = 45)
        assertEquals(12 * 60 + 50, slot)
    }

    @Test
    fun nextFreeSlot_exactFitGap() {
        val occupied = listOf(10 * 60 + 45 to 15)
        val slot = nextFreeSlotMinutes(10 * 60, occupied, durationMin = 45)
        assertEquals(10 * 60, slot)
    }

    @Test
    fun nextFreeSlot_endOfDayWhenLastGapTooShort_returnsNull() {
        val occupied = listOf(22 * 60 to 60)
        val slot = nextFreeSlotMinutes(22 * 60, occupied, durationMin = 90)
        assertNull(slot)
    }

    @Test
    fun nextFreeSlot_noGapReturnsNullInsteadOfOverlap() {
        val occupied = listOf(0 to 24 * 60)
        val slot = nextFreeSlotMinutes(9 * 60, occupied, durationMin = 30)
        assertNull(slot)
    }

    @Test
    fun dateOverride_beatsParsedDate() {
        val resolved =
            resolveQuickCapture(
                QuickCaptureDraft(
                    "gym 18:00",
                    startOverride = LocalTime.of(9, 0),
                    startOverridden = true,
                    dateOverride = today.plusDays(1),
                    dateOverridden = true,
                ),
                today,
                QuickCaptureSaveTarget.Today,
            )
        assertEquals(today.plusDays(1), resolved.date)
        assertEquals("09:00", resolved.startLocalTime)
    }
}
