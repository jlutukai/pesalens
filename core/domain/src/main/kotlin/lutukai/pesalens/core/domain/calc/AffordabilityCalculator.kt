package lutukai.pesalens.core.domain.calc

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.plus
import lutukai.pesalens.core.model.AffordabilityOutcome
import lutukai.pesalens.core.model.Money

object AffordabilityCalculator {
    fun estimate(price: Money, available: Money, monthly: Money, today: LocalDate): AffordabilityOutcome {
        val remaining = price - available
        if (remaining.minor <= 0) return AffordabilityOutcome.Covered
        if (monthly.minor <= 0) return AffordabilityOutcome.NoEndDate(remaining)
        val months = ((remaining.minor + monthly.minor - 1) / monthly.minor).toInt()
        val estimated = YearMonth(today.year, today.month).plus(months, DateTimeUnit.MONTH)
        return AffordabilityOutcome.OnTrack(months, remaining, estimated)
    }

    /** Share of [price] already covered by [available], 0..100; 0 when price is 0. */
    fun percentSetAside(price: Money, available: Money): Int {
        if (price.minor <= 0) return 0
        val percent = available.minor * 100 / price.minor
        return percent.coerceIn(0, 100).toInt()
    }
}
