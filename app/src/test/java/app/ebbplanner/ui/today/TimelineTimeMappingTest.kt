package app.ebbplanner.ui.today

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimelineTimeMappingTest {
    @Test
    fun sixFiveMinuteTasks_alignWithGrid_noDrift_font100() {
        assertSequentialFiveMinuteRun(TimelineLayoutMetrics(1f))
    }

    @Test
    fun sixFiveMinuteTasks_alignWithGrid_noDrift_font200() {
        assertSequentialFiveMinuteRun(TimelineLayoutMetrics(2f))
    }

    @Test
    fun fourThirtyMinuteOverlap_topsMatchGrid_font100() {
        assertOverlappingThirtyMinute(TimelineLayoutMetrics(1f))
    }

    @Test
    fun fourThirtyMinuteOverlap_topsMatchGrid_font200() {
        assertOverlappingThirtyMinute(TimelineLayoutMetrics(2f))
    }

    @Test
    fun nowLine_matchesMapping() {
        val blocks = sequentialFiveMinuteBlocks()
        val metrics = TimelineLayoutMetrics(2f)
        val mapping = buildTimelineTimeMapping(blocks, metrics, 9, 22)
        val now = 10 * 60 + 17
        assertEquals(mapping.y(now), mapping.y(now))
        val gridEleven = mapping.y(11 * 60)
        assertEquals(gridEleven, mapping.y(11 * 60))
    }

    @Test
    fun runningShortTask_pillFitsInDrawnHeight() {
        val metrics = TimelineLayoutMetrics(2f)
        val block =
            TodayScheduledBlock(
                id = "run",
                title = "Quick",
                colorKey = "green",
                iconKey = "task",
                startMinutes = 10 * 60,
                durationMin = 5,
                short = true,
                isCurrent = true,
                minutesLeftLabel = "4 min left",
            )
        val mapping = buildTimelineTimeMapping(listOf(block), metrics, 9, 22)
        val height = mapping.blockHeightDp(block)
        assertTrue(height >= requiredDrawHeightDp(block, metrics))
        assertTrue(height >= minBlockHeightDp(block, metrics) + 22.dp)
    }

    private fun assertSequentialFiveMinuteRun(metrics: TimelineLayoutMetrics) {
        val blocks = sequentialFiveMinuteBlocks()
        val mapping = buildTimelineTimeMapping(blocks, metrics, 9, 22)
        val plan = planTimelineOverlaps(blocks, metrics, 360.dp, mapping)
        blocks.forEach { block ->
            val placement = plan.placements.first { it.first.id == block.id }.second
            assertEquals("top ${block.id}", mapping.y(block.startMinutes), placement.offsetYDp)
            assertEquals("height ${block.id}", mapping.blockHeightDp(block), placement.heightDp)
            val gridY = mapping.y(block.startMinutes)
            assertEquals(gridY, placement.offsetYDp)
        }
        val last = blocks.last()
        val lastEnd = scheduledEndMinutes(last)
        assertEquals(
            "timeline end at ${lastEnd}min must match grid, not cumulative drift",
            mapping.y(lastEnd),
            plan.placements.last().second.offsetYDp + plan.placements.last().second.heightDp,
        )
        assertNonIntersecting(plan)
    }

    private fun assertOverlappingThirtyMinute(metrics: TimelineLayoutMetrics) {
        val start = 10 * 60
        val blocks =
            (1..4).map { i ->
                TodayScheduledBlock(
                    id = "m$i",
                    title = "Meet $i",
                    colorKey = "blue",
                    iconKey = "task",
                    startMinutes = start,
                    durationMin = 30,
                )
            }
        val mapping = buildTimelineTimeMapping(blocks, metrics, 9, 22)
        val plan = planTimelineOverlaps(blocks, metrics, 220.dp, mapping)
        val expectedTop = mapping.y(start)
        plan.placements.forEach { (_, placement) ->
            assertEquals(expectedTop, placement.offsetYDp)
            assertEquals(mapping.blockHeightDp(plan.placements.first().first), placement.heightDp)
        }
        assertEquals(mapping.y(start + 30), expectedTop + plan.placements.first().second.heightDp)
        assertNonIntersecting(plan)
    }

    private fun sequentialFiveMinuteBlocks(): List<TodayScheduledBlock> =
        (0 until 6).map { i ->
            TodayScheduledBlock(
                id = "t$i",
                title = "Task $i",
                colorKey = "green",
                iconKey = "task",
                startMinutes = 10 * 60 + i * 5,
                durationMin = 5,
                short = true,
            )
        }

    private fun assertNonIntersecting(plan: TimelineOverlapPlan) {
        val rects =
            plan.placements.map { (block, placement) ->
                block.id to placement
            }
        for (i in rects.indices) {
            for (j in i + 1 until rects.size) {
                val (idA, a) = rects[i]
                val (idB, b) = rects[j]
                val blockA = plan.placements.first { it.first.id == idA }.first
                val blockB = plan.placements.first { it.first.id == idB }.first
                if (blocksOverlapReal(blockA, blockB)) continue
                val aBottom = a.offsetYDp + a.heightDp
                val bBottom = b.offsetYDp + b.heightDp
                val separated =
                    aBottom <= b.offsetYDp || bBottom <= a.offsetYDp ||
                        a.offsetXDp + a.widthDp <= b.offsetXDp ||
                        b.offsetXDp + b.widthDp <= a.offsetXDp
                assertTrue("$idA intersects $idB", separated)
            }
        }
    }
}