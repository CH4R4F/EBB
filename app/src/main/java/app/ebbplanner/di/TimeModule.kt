package app.ebbplanner.di

import app.ebbplanner.domain.focus.CoroutineFocusTickDelayer
import app.ebbplanner.domain.focus.FocusTickDelayer
import app.ebbplanner.domain.time.EbbClock
import app.ebbplanner.domain.time.SystemEbbClock
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TimeModule {
    @Binds
    @Singleton
    abstract fun bindClock(impl: SystemEbbClock): EbbClock

    @Binds
    @Singleton
    abstract fun bindFocusTickDelayer(impl: CoroutineFocusTickDelayer): FocusTickDelayer
}
