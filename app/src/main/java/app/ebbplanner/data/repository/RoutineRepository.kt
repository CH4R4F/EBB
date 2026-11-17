package app.ebbplanner.data.repository

import app.ebbplanner.data.local.dao.RoutineDao
import app.ebbplanner.data.local.entity.RoutineEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineRepository
    @Inject
    constructor(
        private val routineDao: RoutineDao,
    ) {
        fun observeRoutines(): Flow<List<RoutineEntity>> = routineDao.observeEnabled()

        suspend fun getRoutine(id: String): RoutineEntity? = routineDao.getById(id)

        suspend fun upsert(entity: RoutineEntity) {
            if (routineDao.getById(entity.id) == null) {
                routineDao.insert(entity)
            } else {
                routineDao.update(entity)
            }
        }

        suspend fun delete(id: String) {
            routineDao.delete(id)
        }

        suspend fun createDefault(
            title: String,
            repeatRule: String = RoutineEntity.REPEAT_WEEKDAYS,
            startLocalTime: String = "09:00",
            durationMin: Int = 30,
        ): RoutineEntity {
            val now = System.currentTimeMillis()
            val entity =
                RoutineEntity(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    repeatRule = repeatRule,
                    weeklyDays = "",
                    startLocalTime = startLocalTime,
                    durationMin = durationMin,
                    createdAt = now,
                    updatedAt = now,
                )
            routineDao.insert(entity)
            return entity
        }
    }
