package app.ebbplanner.data.repository

import app.ebbplanner.data.local.dao.TaskDao
import app.ebbplanner.data.local.entity.TaskEntity

/**
 * Applies a routine-linked task update and recovers when SQLITE uniqueness triggers ignore the write.
 */
internal suspend fun TaskDao.updateRoutineTask(task: TaskEntity): Int {
    var rows = update(task)
    if (rows > 0 || task.sourceRoutineId == null) return rows
    val detached =
        task.copy(
            routineInstanceDetached = true,
            routineOccurrenceDate = null,
        )
    rows = update(detached)
    return rows
}

internal suspend fun TaskDao.undeleteRoutineTask(
    id: String,
    updatedAt: Long,
): Int {
    val rows = undeleteTask(id, updatedAt)
    if (rows > 0) return rows
    return undeleteTaskClearingOccurrenceKey(id, updatedAt)
}