package app.ebbplanner.ui.today

import androidx.compose.ui.unit.dp
import app.ebbplanner.data.local.entity.TaskEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TimelineAdjacentOverlapTest {
    @Test
    fun kiloLima_atFont100_noOverlapHint_noOverflowChip() {
        assertScenario(1f, "kilo", "11:00", 30, "lima", "11:35", 45)
    }

    @Test
    fun kiloLima_atFont200_noOverlapHint_noOverflowChip() {
        assertScenario(2f, "kilo", "11:00", 30, "lima", "11:35", 45)
    }

    @Test
    fun unoVole_atFont100_noOverlapHint_noOverflowChip() {
        assertScenario(1f, "uno", "10:00", 5, "vole", "10:10", 5)
    }

    @Test
    fun unoVole_atFont200_noOverlapHint_noOverflowChip() {
        assertScenario(2f, "uno", "10:00", 5, "vole", "10:10", 5)
    }

    private fun assertScenario(
        fontScale: Float,
        idA: String,
        startA: String,
        durA: Int,
        idB: String,
        startB: String,
        durB: Int,
    ) {
        val today = LocalDate.of(2025, 11, 17)
        val state =
            TodayUiState(
                date = today,
                scheduledTasks =
                    listOf(
                        task(today, idA, startA, durA),
                        task(today, idB, startB, durB),
                    ),
                isLoading = false,
            )
        val mapped = TodayPresentationMapper.from(state, 10 * 60, fontScale)
        assertNull(mapped.overlapHint)
        val plan =
            planTimelineOverlaps(
                mapped.blocks,
                TimelineLayoutMetrics(fontScale = fontScale),
                300.dp,
                mapped.timelineStartHour,
            )
        assertTrue(plan.overflow.isEmpty())
        plan.placements.forEach { (_, placement) ->
            assertEquals(300.dp, placement.widthDp)
        }
    }

    private fun task(
        date: LocalDate,
        id: String,
        start: String,
        durationMin: Int,
    ): TaskEntity =
        TaskEntity(
            id = id,
            title = id,
            placement = TaskEntity.PLACEMENT_SCHEDULED,
            date = date.toString(),
            startLocalTime = start,
            durationMin = durationMin,
            createdAt = 1,
            updatedAt = 1,
        )
}