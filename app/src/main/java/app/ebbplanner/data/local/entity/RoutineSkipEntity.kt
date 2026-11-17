package app.ebbplanner.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "routine_skips",
    primaryKeys = ["routineId", "occurrenceDate"],
)
data class RoutineSkipEntity(
    val routineId: String,
    val occurrenceDate: String,
)