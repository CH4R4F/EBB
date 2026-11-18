package app.ebbplanner.ui.today

import org.junit.Assert.assertEquals
import org.junit.Test

class TimelineLayoutTest {
    @Test
    fun overlapComponents_splitsNonOverlappingGroups() {
        val blocks =
            listOf(
                block("solo", 9 * 60, 30),
                block("a", 10 * 60, 30),
                block("b", 10 * 60 + 5, 30),
            )
        val metrics = TimelineLayoutMetrics(fontScale = 1f)
        val groups = overlapComponents(blocks, metrics)
        assertEquals(2, groups.size)
        val meta = applyOverlapMetadata(blocks, TimelineLayoutMetrics(fontScale = 1f))
        assertEquals(1, meta.first { it.id == "solo" }.overlapLaneCount)
        assertEquals(2, meta.first { it.id == "a" }.overlapLaneCount)
    }

    @Test
    fun kiloLima_drawOverlap_atFont2_assignsSeparateLanes() {
        val blocks =
            listOf(
                block("kilo", 11 * 60, 30, meta = true),
                block("lima", 11 * 60 + 35, 45, meta = true),
            )
        val metrics = TimelineLayoutMetrics(fontScale = 2f)
        val meta = applyOverlapMetadata(blocks, metrics)
        assertEquals(2, meta.first { it.id == "kilo" }.overlapLaneCount)
        assertEquals(0, meta.first { it.id == "kilo" }.overlapLane)
        assertEquals(1, meta.first { it.id == "lima" }.overlapLane)
    }

    private fun block(
        id: String,
        start: Int,
        duration: Int,
        meta: Boolean = true,
    ): TodayScheduledBlock =
        TodayScheduledBlock(
            id = id,
            title = id,
            colorKey = "green",
            iconKey = "task",
            startMinutes = start,
            durationMin = duration,
            metaTimeRange = if (meta) "t" else null,
            metaDuration = if (meta) "$duration min" else null,
        )
}