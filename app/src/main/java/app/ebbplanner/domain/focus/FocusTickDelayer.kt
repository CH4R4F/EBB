package app.ebbplanner.domain.focus

import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

fun interface FocusTickDelayer {
    suspend fun delayMs(ms: Long)
}

@Singleton
class CoroutineFocusTickDelayer
    @Inject
    constructor() : FocusTickDelayer {
        override suspend fun delayMs(ms: Long) {
            delay(ms)
        }
    }
