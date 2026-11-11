package lutukai.pesalens.core.testing

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

class TestClock(var now: Instant = Instant.parse("2026-11-10T09:41:00Z")) : Clock {
    override fun now(): Instant = now

    fun advanceBy(duration: Duration) {
        now += duration
    }
}
