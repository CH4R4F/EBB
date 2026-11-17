package app.ebbplanner.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import app.ebbplanner.data.local.dao.DayPlanDao
import app.ebbplanner.data.local.dao.ReminderDao
import app.ebbplanner.data.local.dao.RoutineDao
import app.ebbplanner.data.local.dao.RoutineSkipDao
import app.ebbplanner.data.local.dao.SubtaskDao
import app.ebbplanner.data.local.dao.TaskDao
import app.ebbplanner.data.local.entity.DayPlanEntity
import app.ebbplanner.data.local.entity.ReminderEntity
import app.ebbplanner.data.local.entity.RoutineEntity
import app.ebbplanner.data.local.entity.RoutineSkipEntity
import app.ebbplanner.data.local.entity.SubtaskEntity
import app.ebbplanner.data.local.entity.TaskEntity

@Database(
    entities = [
        TaskEntity::class,
        DayPlanEntity::class,
        ReminderEntity::class,
        SubtaskEntity::class,
        RoutineEntity::class,
        RoutineSkipEntity::class,
    ],
    version = 7,
    exportSchema = false,
)
abstract class EbbDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    abstract fun dayPlanDao(): DayPlanDao

    abstract fun reminderDao(): ReminderDao

    abstract fun subtaskDao(): SubtaskDao

    abstract fun routineDao(): RoutineDao

    abstract fun routineSkipDao(): RoutineSkipDao
}
