package app.ebbplanner.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.ebbplanner.data.local.entity.RoutineSkipEntity

@Dao
interface RoutineSkipDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(skip: RoutineSkipEntity)

    @Query("SELECT 1 FROM routine_skips WHERE routineId = :routineId AND occurrenceDate = :date LIMIT 1")
    suspend fun isSkipped(
        routineId: String,
        date: String,
    ): Int?

    @Query("DELETE FROM routine_skips WHERE routineId = :routineId")
    suspend fun deleteForRoutine(routineId: String)

    @Query("DELETE FROM routine_skips WHERE routineId = :routineId AND occurrenceDate = :date")
    suspend fun deleteSkip(
        routineId: String,
        date: String,
    )
}