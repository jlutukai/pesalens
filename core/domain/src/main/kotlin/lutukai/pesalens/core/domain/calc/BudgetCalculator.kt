package lutukai.pesalens.core.domain.calc

import lutukai.pesalens.core.model.BudgetTotals
import lutukai.pesalens.core.model.Money

object BudgetCalculator {
    fun totals(income: Money, limits: List<Money>): BudgetTotals {
        val total = limits.fold(Money.Zero) { acc, m -> acc + m }
        val difference = income - total
        val ratio = if (income.minor <= 0) {
            if (total.minor > 0) 1f else 0f
        } else {
            (total.minor.toDouble() / income.minor.toDouble()).toFloat().coerceIn(0f, 1f)
        }
        return BudgetTotals(total, difference, difference.minor < 0, ratio)
    }
}
