package app.ebbplanner.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminders",
    indices = [Index(value = ["taskId"])],
)
data class ReminderEntity(
    @PrimaryKey val id: String,
    val taskId: String,
    val fireAtEpochMillis: Long,
    val kind: String,
    val title: String,
    val exact: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)
