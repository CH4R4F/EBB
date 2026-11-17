package app.ebbplanner.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import app.ebbplanner.data.local.dao.DayPlanDao
import app.ebbplanner.data.local.dao.ReminderDao
import app.ebbplanner.data.local.dao.TaskDao
import app.ebbplanner.data.local.entity.DayPlanEntity
import app.ebbplanner.data.local.entity.ReminderEntity
import app.ebbplanner.data.local.entity.TaskEntity

@Database(
    entities = [
        TaskEntity::class,
        DayPlanEntity::class,
        ReminderEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class EbbDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    abstract fun dayPlanDao(): DayPlanDao

    abstract fun reminderDao(): ReminderDao
}
