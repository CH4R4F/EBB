package app.ebbplanner.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import app.ebbplanner.data.local.entity.RoutineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines WHERE enabled = 1 ORDER BY title COLLATE NOCASE")
    fun observeEnabled(): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routines WHERE enabled = 1 ORDER BY title COLLATE NOCASE")
    suspend fun listEnabled(): List<RoutineEntity>

    @Query("SELECT * FROM routines ORDER BY title COLLATE NOCASE")
    fun observeAll(): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routines WHERE id = :id")
    suspend fun getById(id: String): RoutineEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RoutineEntity)

    @Update
    suspend fun update(entity: RoutineEntity)

    @Query("DELETE FROM routines WHERE id = :id")
    suspend fun delete(id: String)
}
