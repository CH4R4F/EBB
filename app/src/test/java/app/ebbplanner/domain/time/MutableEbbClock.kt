package app.ebbplanner.domain.time

import java.time.Instant
import java.time.ZoneId

class MutableEbbClock(
    private var instant: Instant,
    private val zone: ZoneId = ZoneId.of("UTC"),
) : EbbClock {
    private var millisProvider: (() -> Long)? = null

    fun setInstant(value: Instant) {
        instant = value
    }

    fun advanceMinutes(minutes: Int) {
        instant = instant.plusSeconds(minutes * 60L)
    }

    fun setMillisProvider(provider: () -> Long) {
        millisProvider = provider
    }

    fun clearMillisProvider() {
        millisProvider = null
    }

    override fun instant(): Instant = Instant.ofEpochMilli(millis())

    override fun zoneId(): ZoneId = zone

    override fun millis(): Long = millisProvider?.invoke() ?: instant.toEpochMilli()
}
