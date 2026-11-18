package app.ebbplanner.ui.today

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import androidx.compose.ui.unit.dp

class TimelineLayoutTest {
    @Test
    fun overlapComponents_splitsNonOverlappingGroups() {
        val blocks =
            listOf(
                block("solo", 9 * 60, 30),
                block("a", 10 * 60, 30),
                block("b", 10 * 60 + 5, 30),
            )
        val groups = overlapComponents(blocks)
        assertEquals(2, groups.size)
        val meta = applyOverlapMetadata(blocks)
        assertEquals(1, meta.first { it.id == "solo" }.overlapLaneCount)
        assertEquals(2, meta.first { it.id == "a" }.overlapLaneCount)
    }

    @Test
    fun kiloLima_notGrouped_sameGapAsScheduled() {
        val blocks =
            listOf(
                block("kilo", 11 * 60, 30, meta = true),
                block("lima", 11 * 60 + 35, 45, meta = true),
            )
        assertFalse(hasRealTimeOverlap(blocks))
        val groups = overlapComponents(blocks)
        assertEquals(2, groups.size)
        val metrics = TimelineLayoutMetrics(fontScale = 2f)
        val plan = buildTimelineOverlapPlan(blocks, metrics, 300.dp, 9)
        assertTrue(plan.overflow.isEmpty())
        assertEquals(2, plan.placements.size)
        plan.placements.forEach { (_, placement) ->
            assertEquals(300.dp, placement.widthDp)
        }
    }

    @Test
    fun unoVole_shortGap_noRealOverlap() {
        val blocks =
            listOf(
                block("uno", 10 * 60, 5),
                block("vole", 10 * 60 + 10, 5),
            )
        assertFalse(hasRealTimeOverlap(blocks))
        val plan = buildTimelineOverlapPlan(blocks, TimelineLayoutMetrics(2f), 300.dp, 9)
        assertTrue(plan.overflow.isEmpty())
    }

    @Test
    fun chipAnchorsAtGroupStart_heightWithinGroupExtent() {
        val blocks =
            (1..4).map { i ->
                block("lane-$i", 10 * 60 + (i - 1) * 5, 30)
            }
        val metrics = TimelineLayoutMetrics(1f)
        val mapping = buildTimelineTimeMapping(blocks, metrics, 9, 22)
        val plan = planTimelineOverlaps(blocks, metrics, 300.dp, mapping)
        val cluster = plan.overflow.single()
        val groupTop = blocks.minOf { mapping.y(it.startMinutes) }
        val groupBottom = blocks.maxOf { mapping.y(scheduledEndMinutes(it)) }
        assertEquals(groupTop, cluster.offsetYDp)
        assertEquals(groupBottom, cluster.offsetYDp + cluster.heightDp)
    }

    @Test
    fun overflowChipCount_matchesHiddenOnly() {
        val blocks =
            (0 until 5).map { i ->
                block("lane-$i", 10 * 60 + i * 5, 30)
            }
        val plan = buildTimelineOverlapPlan(blocks, TimelineLayoutMetrics(1f), 300.dp, 9)
        val cluster = plan.overflow.single()
        assertEquals(3, cluster.hiddenBlocks.size)
        assertEquals(5, cluster.allGroupBlocks.size)
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