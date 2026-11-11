package lutukai.pesalens.core.testing

import lutukai.pesalens.core.model.Money
import kotlin.test.Test
import kotlin.test.assertEquals

class SampleFinanceTest {
    @Test
    fun november_matchesPrototype() {
        val summary = SampleFinance.november
        assertEquals(Money.kes(65_000), summary.total)
        assertEquals(listOf(38, 23, 12, 8, 11, 8), summary.categories.map { it.sharePercent })
        assertEquals(listOf("rent", "food", "transport", "utilities", "shopping", "other"), summary.categories.map { it.category.id })
        assertEquals("food", summary.focusCategoryId)
    }

    @Test
    fun foodTransactions_totalFifteenThousand() {
        val food = SampleFinance.categoryTransactions("food")
        assertEquals(4, food.transactions.size)
        assertEquals(Money.kes(15_000), food.total)
        assertEquals(23, food.sharePercent)
        assertEquals(6, food.transactions.first { it.merchant == "Lunches" }.entryCount)
    }

    @Test
    fun draft_isBudget0() {
        assertEquals(Money.kes(100_000), SampleFinance.draft.income)
        assertEquals(listOf(25_000L, 15_000L, 10_000L, 7_000L, 8_000L, 10_000L), SampleFinance.draft.lines.map { it.limit.minor / 100 })
    }
}
