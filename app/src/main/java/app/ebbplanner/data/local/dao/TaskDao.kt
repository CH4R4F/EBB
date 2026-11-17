package app.ebbplanner.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import app.ebbplanner.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Suppress("TooManyFunctions")
@Dao
interface TaskDao {
    @Query(
        """
        SELECT * FROM tasks
        WHERE deletedAt IS NULL AND date = :date
        ORDER BY
            CASE placement
                WHEN 'scheduled' THEN 0
                WHEN 'anytime' THEN 1
                ELSE 2
            END,
            startLocalTime ASC,
            createdAt ASC
        """,
    )
    fun observeForDate(date: String): Flow<List<TaskEntity>>

    @Query("SELECT COUNT(*) FROM tasks WHERE deletedAt IS NULL")
    suspend fun countActive(): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE deletedAt IS NULL AND date = :date")
    suspend fun countForDate(date: String): Int

    @Query("SELECT * FROM tasks WHERE id = :id AND deletedAt IS NULL LIMIT 1")
    suspend fun getById(id: String): TaskEntity?

    @Query(
        """
        SELECT * FROM tasks
        WHERE deletedAt IS NULL AND sourceRoutineId = :routineId AND routineOccurrenceDate = :occurrenceDate
        LIMIT 1
        """,
    )
    suspend fun getRoutineOccurrence(
        routineId: String,
        occurrenceDate: String,
    ): TaskEntity?

    @Query(
        """
        SELECT COUNT(*) > 0 FROM tasks
        WHERE deletedAt IS NULL AND sourceRoutineId = :routineId AND date = :date
        AND (
            routineOccurrenceDate IS NULL
            OR routineOccurrenceDate = ''
            OR routineOccurrenceDate = :occurrenceDate
        )
        """,
    )
    suspend fun blocksRoutineOccurrenceOnDate(
        routineId: String,
        date: String,
        occurrenceDate: String,
    ): Boolean

    @Query(
        """
        SELECT * FROM tasks
        WHERE deletedAt IS NULL AND sourceRoutineId = :routineId
        AND date >= :fromDateInclusive AND status != 'done'
        """,
    )
    suspend fun listOpenRoutineInstancesFrom(
        routineId: String,
        fromDateInclusive: String,
    ): List<TaskEntity>

    @Query(
        """
        SELECT * FROM tasks
        WHERE deletedAt IS NULL AND placement = 'inbox'
        ORDER BY createdAt DESC
        """,
    )
    fun observeInbox(): Flow<List<TaskEntity>>

    @Query(
        """
        SELECT * FROM tasks
        WHERE deletedAt IS NULL AND date >= :startInclusive AND date <= :endInclusive
        ORDER BY date ASC, startLocalTime ASC
        """,
    )
    fun observeBetween(
        startInclusive: String,
        endInclusive: String,
    ): Flow<List<TaskEntity>>

    @Query(
        """
        SELECT * FROM tasks
        WHERE deletedAt IS NULL
        AND placement = 'scheduled'
        AND startLocalTime IS NOT NULL
        AND reminderEnabled = 1
        """,
    )
    suspend fun getActiveScheduledWithReminders(): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRoutineInstanceIfAbsent(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<TaskEntity>)

    @Update
    suspend fun update(task: TaskEntity)

    @Query("UPDATE tasks SET deletedAt = :deletedAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDelete(
        id: String,
        deletedAt: Long,
        updatedAt: Long,
    )
}
