package app.ebbplanner.ui.today

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.ebbplanner.data.local.entity.TaskEntity
import com.android.ide.common.rendering.api.SessionParams
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test

class TimelineOverlapPaparazziScale100Test {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_6.copy(fontScale = 1f),
            renderingMode = SessionParams.RenderingMode.SHRINK,
        )

    @Test
    fun timeline_overlap_four_lanes_scale_100() {
        paparazzi.snapshot("timeline_overlap_four_lanes_scale_100") {
            TimelineOverlapPreview(state = overlapFourLaneState(fontScale = 1f))
        }
    }

    @Test
    fun timeline_overlap_five_lanes_overflow_scale_100() {
        paparazzi.snapshot("timeline_overlap_five_lanes_overflow_scale_100") {
            TimelineOverlapPreview(state = overlapFiveLaneState(fontScale = 1f))
        }
    }

    @Test
    fun timeline_kilo_lima_adjacent_scale_100() {
        paparazzi.snapshot("timeline_kilo_lima_adjacent_scale_100") {
            TimelineOverlapPreview(state = kiloLimaState(fontScale = 1f))
        }
    }
}

class TimelineOverlapPaparazziScale200Test {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_6.copy(fontScale = 2f),
            renderingMode = SessionParams.RenderingMode.SHRINK,
        )

    @Test
    fun timeline_overlap_four_lanes_scale_200() {
        paparazzi.snapshot("timeline_overlap_four_lanes_scale_200") {
            TimelineOverlapPreview(state = overlapFourLaneState(fontScale = 2f))
        }
    }

    @Test
    fun timeline_overlap_five_lanes_overflow_scale_200() {
        paparazzi.snapshot("timeline_overlap_five_lanes_overflow_scale_200") {
            TimelineOverlapPreview(state = overlapFiveLaneState(fontScale = 2f))
        }
    }

    @Test
    fun timeline_kilo_lima_adjacent_scale_200() {
        paparazzi.snapshot("timeline_kilo_lima_adjacent_scale_200") {
            TimelineOverlapPreview(state = kiloLimaState(fontScale = 2f))
        }
    }
}

private fun overlapFourLaneState(fontScale: Float): TodayPresentationState {
    val today = LocalDate.of(2025, 11, 17)
    val tasks =
        listOf(
            task(today, "lane-a", "10:00", 30),
            task(today, "lane-b", "10:05", 30),
            task(today, "lane-c", "10:10", 30),
            task(today, "lane-d", "10:15", 30),
        )
    return TodayPresentationMapper.from(TodayUiState(date = today, scheduledTasks = tasks, isLoading = false), 10 * 60, fontScale)
}

private fun overlapFiveLaneState(fontScale: Float): TodayPresentationState {
    val today = LocalDate.of(2025, 11, 17)
    val tasks =
        listOf(
            task(today, "lane-a", "10:00", 30),
            task(today, "lane-b", "10:05", 30),
            task(today, "lane-c", "10:10", 30),
            task(today, "lane-d", "10:15", 30),
            task(today, "lane-e", "10:20", 30),
        )
    return TodayPresentationMapper.from(TodayUiState(date = today, scheduledTasks = tasks, isLoading = false), 10 * 60, fontScale)
}

private fun kiloLimaState(fontScale: Float): TodayPresentationState {
    val today = LocalDate.of(2025, 11, 17)
    val tasks =
        listOf(
            task(today, "kilo", "11:00", 30),
            task(today, "lima", "11:35", 45),
        )
    return TodayPresentationMapper.from(TodayUiState(date = today, scheduledTasks = tasks, isLoading = false), 11 * 60, fontScale)
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