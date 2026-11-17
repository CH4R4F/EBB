package app.ebbplanner.domain.time

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

interface EbbClock {
    fun instant(): Instant

    fun today(): LocalDate = instant().atZone(zoneId()).toLocalDate()

    fun localTime(): LocalTime = instant().atZone(zoneId()).toLocalTime()

    fun nowMinutes(): Int = localTime().hour * 60 + localTime().minute

    fun zoneId(): ZoneId

    fun millis(): Long = instant().toEpochMilli()
}

@Singleton
class SystemEbbClock
    @Inject
    constructor() : EbbClock {
        private val zone: ZoneId = ZoneId.systemDefault()

        override fun instant(): Instant = Instant.now()

        override fun zoneId(): ZoneId = zone
    }
