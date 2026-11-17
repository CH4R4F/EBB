package app.ebbplanner.di

import android.content.Context
import androidx.room.Room
import app.ebbplanner.data.local.EbbDatabase
import app.ebbplanner.data.local.dao.DayPlanDao
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
        ).build()

    @Provides
    fun provideTaskDao(database: EbbDatabase): TaskDao = database.taskDao()

    @Provides
    fun provideDayPlanDao(database: EbbDatabase): DayPlanDao = database.dayPlanDao()
}
