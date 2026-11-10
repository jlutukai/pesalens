package lutukai.pesalens.core.domain.calc

import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import lutukai.pesalens.core.model.AffordabilityOutcome
import lutukai.pesalens.core.model.Money.Companion.kes
import kotlin.test.Test
import kotlin.test.assertEquals

class AffordabilityCalculatorTest {
    private val today = LocalDate(2026, 11, 10)

    @Test
    fun estimate_onTrack() {
        assertEquals(
            AffordabilityOutcome.OnTrack(6, kes(60_000), YearMonth(2027, 5)),
            AffordabilityCalculator.estimate(kes(90_000), kes(30_000), kes(10_000), today),
        )
    }

    @Test
    fun estimate_higherMonthly() {
        assertEquals(
            AffordabilityOutcome.OnTrack(4, kes(60_000), YearMonth(2027, 3)),
            AffordabilityCalculator.estimate(kes(90_000), kes(30_000), kes(15_000), today),
        )
    }

    @Test
    fun estimate_covered() {
        assertEquals(
            AffordabilityOutcome.Covered,
            AffordabilityCalculator.estimate(kes(90_000), kes(90_000), kes(10_000), today),
        )
    }

    @Test
    fun estimate_noMonthly_noEndDate() {
        assertEquals(
            AffordabilityOutcome.NoEndDate(kes(60_000)),
            AffordabilityCalculator.estimate(kes(90_000), kes(30_000), kes(0), today),
        )
    }

    @Test
    fun percentSetAside() {
        assertEquals(33, AffordabilityCalculator.percentSetAside(kes(90_000), kes(30_000)))
        assertEquals(0, AffordabilityCalculator.percentSetAside(kes(0), kes(30_000)))
        assertEquals(100, AffordabilityCalculator.percentSetAside(kes(100), kes(300)))
    }
}
