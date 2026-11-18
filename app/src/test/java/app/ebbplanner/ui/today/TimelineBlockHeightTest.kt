package app.ebbplanner.ui.today

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimelineBlockHeightTest {
    @Test
    fun kiloLima_unoVole_font200_keepMinHeight_noIntersectingRects() {
        val metrics = TimelineLayoutMetrics(fontScale = 2f)
        val kiloLima =
            listOf(
                block("kilo", 11 * 60, 30, meta = true),
                block("lima", 11 * 60 + 35, 45, meta = true),
            )
        assertLayouts(kiloLima, metrics, 9)

        val unoVole =
            listOf(
                block("uno", 10 * 60, 5, meta = false),
                block("vole", 10 * 60 + 10, 5, meta = false),
            )
        assertLayouts(unoVole, metrics, 9)
    }

    private fun assertLayouts(
        blocks: List<TodayScheduledBlock>,
        metrics: TimelineLayoutMetrics,
        startHour: Int,
    ) {
        val mapping = buildTimelineTimeMapping(blocks, metrics, startHour, 22)
        val plan = planTimelineOverlaps(blocks, metrics, 360.dp, mapping)
        blocks.forEach { block ->
            val placement = plan.placements.first { it.first.id == block.id }.second
            val min = minBlockHeightDp(block, metrics)
            assertTrue("${block.id} height ${placement.heightDp} < min $min", placement.heightDp >= min)
            assertEquals(mapping.y(block.startMinutes), placement.offsetYDp)
        }
        val sorted = blocks.sortedBy { it.startMinutes }
        for (i in sorted.indices) {
            for (j in i + 1 until sorted.size) {
                val a = plan.placements.first { it.first.id == sorted[i].id }.second
                val b = plan.placements.first { it.first.id == sorted[j].id }.second
                val aBottom = a.offsetYDp + a.heightDp
                assertTrue(
                    "${sorted[i].id} draws into ${sorted[j].id}",
                    aBottom <= b.offsetYDp || blocksOverlapReal(sorted[i], sorted[j]),
                )
            }
        }
    }

    private fun block(
        id: String,
        start: Int,
        duration: Int,
        meta: Boolean,
    ): TodayScheduledBlock =
        TodayScheduledBlock(
            id = id,
            title = id,
            colorKey = "green",
            iconKey = "task",
            startMinutes = start,
            durationMin = duration,
            metaTimeRange = if (meta) "10:00–10:30" else null,
            metaDuration = if (meta) "$duration min" else null,
        )
}