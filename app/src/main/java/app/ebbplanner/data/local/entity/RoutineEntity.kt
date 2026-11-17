package app.ebbplanner.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey val id: String,
    val title: String,
    val repeatRule: String,
    val weeklyDays: String = "",
    val startLocalTime: String,
    val durationMin: Int,
    val colorKey: String = "slate",
    val iconKey: String? = null,
    val enabled: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long,
) {
    companion object {
        const val REPEAT_DAILY = "daily"
        const val REPEAT_WEEKDAYS = "weekdays"
        const val REPEAT_WEEKLY = "weekly"
    }
}
