package app.ebbplanner.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    indices = [
        Index(value = ["date"]),
        Index(value = ["placement"]),
    ],
)
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val notes: String = "",
    val status: String = STATUS_OPEN,
    val placement: String = PLACEMENT_INBOX,
    val date: String? = null,
    val startLocalTime: String? = null,
    val zoneId: String = "UTC",
    val durationMin: Int = DEFAULT_DURATION_MIN,
    val colorKey: String = "slate",
    val iconKey: String? = null,
    val energy: String? = null,
    val pinned: Boolean = false,
    val reminderEnabled: Boolean = true,
    val sourceRoutineId: String? = null,
    val routineOccurrenceDate: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
) {
    companion object {
        const val STATUS_OPEN = "open"
        const val STATUS_DONE = "done"
        const val PLACEMENT_INBOX = "inbox"
        const val PLACEMENT_ANYTIME = "anytime"
        const val PLACEMENT_SCHEDULED = "scheduled"
        const val PLACEMENT_SOMEDAY = "someday"
        const val DEFAULT_DURATION_MIN = 15
    }
}
