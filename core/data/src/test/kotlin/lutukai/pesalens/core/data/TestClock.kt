package lutukai.pesalens.core.data

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

/** Local to this module's tests: `:core:data` does not depend on `:core:testing`. */
internal class TestClock(var now: Instant = Instant.parse("2026-11-10T09:41:00Z")) : Clock {
    override fun now(): Instant = now

    fun advanceBy(d: Duration) {
        now += d
    }
}
