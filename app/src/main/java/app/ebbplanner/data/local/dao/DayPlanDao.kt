package app.ebbplanner.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import app.ebbplanner.data.local.entity.DayPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DayPlanDao {
    @Query("SELECT * FROM day_plans WHERE date = :date LIMIT 1")
    fun observeForDate(date: String): Flow<DayPlanEntity?>

    @Query("SELECT * FROM day_plans WHERE date = :date LIMIT 1")
    suspend fun getForDate(date: String): DayPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plan: DayPlanEntity)

    @Update
    suspend fun update(plan: DayPlanEntity)
}
