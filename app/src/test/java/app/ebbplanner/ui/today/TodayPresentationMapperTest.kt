package app.ebbplanner.ui.today

import app.ebbplanner.data.local.entity.TaskEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class TodayPresentationMapperTest {
    @Test
    fun from_detectsOverlapAndOverfull() {
        val today = LocalDate.now()
        val now = 10 * 60
        val state =
            TodayUiState(
                date = today,
                dateLabel = "Mon 17 Nov",
                bedMinutes = 11 * 60,
                scheduledTasks =
                    listOf(
                        taskAt(today, "10:00", 60, "a"),
                        taskAt(today, "10:30", 60, "b"),
                    ),
                isLoading = false,
            )
        val mapped = TodayPresentationMapper.from(state, now)
        assertNotNull(mapped.overlapHint)
        assertNotNull(mapped.bannerMessage)
    }

    @Test
    fun from_expandsTimelineBeforeNineWhenTasksStartEarly() {
        val today = LocalDate.now()
        val state =
            TodayUiState(
                date = today,
                scheduledTasks = listOf(taskAt(today, "07:30", 45, "early")),
                isLoading = false,
            )
        val mapped = TodayPresentationMapper.from(state, 8 * 60)
        assertEquals(7, mapped.timelineStartHour)
        assertEquals(17, mapped.timelineEndHour)
    }

    @Test
    fun assignOverlapLanes_usesDrawnHeightForShortAdjacentBlocks() {
        val today = LocalDate.now()
        val state =
            TodayUiState(
                date = today,
                scheduledTasks =
                    listOf(
                        taskAt(today, "10:00", 5, "short-a"),
                        taskAt(today, "10:03", 5, "short-b"),
                    ),
                isLoading = false,
            )
        val mapped = TodayPresentationMapper.from(state, 10 * 60)
        val a = mapped.blocks.first { it.id == "short-a" }
        val b = mapped.blocks.first { it.id == "short-b" }
        assertEquals(0, a.overlapLane)
        assertEquals(1, b.overlapLane)
    }

    @Test
    fun from_anytimeChipsIncludeIds() {
        val today = LocalDate.now()
        val task =
            TaskEntity(
                id = "chip-1",
                title = "Water",
                placement = TaskEntity.PLACEMENT_ANYTIME,
                date = today.toString(),
                createdAt = 1,
                updatedAt = 1,
            )
        val state = TodayUiState(date = today, anytimeTasks = listOf(task), isLoading = false)
        val mapped = TodayPresentationMapper.from(state, 12 * 60)
        assertNull(mapped.overlapHint)
        assertEquals("chip-1", mapped.anytimeChips.single().id)
    }

    private fun taskAt(
        date: LocalDate,
        start: String,
        duration: Int,
        id: String,
    ): TaskEntity =
        TaskEntity(
            id = id,
            title = id,
            placement = TaskEntity.PLACEMENT_SCHEDULED,
            date = date.toString(),
            startLocalTime = start,
            durationMin = duration,
            createdAt = 1,
            updatedAt = 1,
        )
}
