package app.ebbplanner.di

import android.content.Context
import androidx.room.Room
import app.ebbplanner.data.local.EbbDatabase
import app.ebbplanner.data.local.MIGRATION_2_3
import app.ebbplanner.data.local.MIGRATION_3_4
import app.ebbplanner.data.local.dao.DayPlanDao
import app.ebbplanner.data.local.dao.ReminderDao
import app.ebbplanner.data.local.dao.RoutineDao
import app.ebbplanner.data.local.dao.SubtaskDao
import app.ebbplanner.data.local.dao.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): EbbDatabase =
        Room.databaseBuilder(
            context,
            EbbDatabase::class.java,
            "ebb.db",
        )
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
            .build()

    @Provides
    fun provideTaskDao(database: EbbDatabase): TaskDao = database.taskDao()

    @Provides
    fun provideDayPlanDao(database: EbbDatabase): DayPlanDao = database.dayPlanDao()

    @Provides
    fun provideReminderDao(database: EbbDatabase): ReminderDao = database.reminderDao()

    @Provides
    fun provideSubtaskDao(database: EbbDatabase): SubtaskDao = database.subtaskDao()

    @Provides
    fun provideRoutineDao(database: EbbDatabase): RoutineDao = database.routineDao()
}
