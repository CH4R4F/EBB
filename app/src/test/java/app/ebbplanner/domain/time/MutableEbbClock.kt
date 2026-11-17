package app.ebbplanner.domain.time

import java.time.Instant
import java.time.ZoneId

class MutableEbbClock(
    private var instant: Instant,
    private val zone: ZoneId = ZoneId.of("UTC"),
) : EbbClock {
    fun setInstant(value: Instant) {
        instant = value
    }

    fun advanceMinutes(minutes: Int) {
        instant = instant.plusSeconds(minutes * 60L)
    }

    override fun instant(): Instant = instant

    override fun zoneId(): ZoneId = zone

    override fun millis(): Long = instant.toEpochMilli()
}
