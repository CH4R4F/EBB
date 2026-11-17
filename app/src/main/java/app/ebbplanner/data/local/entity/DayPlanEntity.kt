package app.ebbplanner.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "day_plans",
    indices = [Index(value = ["date"], unique = true)],
)
data class DayPlanEntity(
    @PrimaryKey val id: String,
    val date: String,
    val wakeMinutes: Int? = null,
    val bedMinutes: Int? = null,
    val reviewedAt: Long? = null,
    val note: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
)
