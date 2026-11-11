package lutukai.pesalens.core.domain.calc

import lutukai.pesalens.core.model.Money.Companion.kes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BudgetCalculatorTest {
    @Test
    fun totals_underIncome() {
        val t = BudgetCalculator.totals(
            kes(100_000),
            listOf(25_000L, 15_000, 10_000, 7_000, 8_000, 10_000).map { kes(it) },
        )
        assertEquals(kes(75_000), t.total)
        assertEquals(kes(25_000), t.difference)
        assertFalse(t.isOver)
        assertEquals(0.75f, t.allocatedRatio)
    }

    @Test
    fun totals_overIncome() {
        val t = BudgetCalculator.totals(
            kes(100_000),
            listOf(25_000L, 15_000, 10_000, 7_000, 8_000, 45_000).map { kes(it) },
        )
        assertEquals(kes(-10_000), t.difference)
        assertTrue(t.isOver)
        assertEquals(1f, t.allocatedRatio)
    }

    @Test
    fun budgetTotals_zeroIncome_isOverWhenAnyLimit() {
        val t = BudgetCalculator.totals(kes(0), listOf(kes(1)))
        assertTrue(t.isOver)
        assertEquals(1f, t.allocatedRatio)
    }
}
