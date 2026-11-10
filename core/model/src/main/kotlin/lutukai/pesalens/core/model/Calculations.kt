package lutukai.pesalens.core.model

import kotlinx.datetime.YearMonth

sealed interface AffordabilityOutcome {
    data object Covered : AffordabilityOutcome

    data class NoEndDate(val remaining: Money) : AffordabilityOutcome

    data class OnTrack(val months: Int, val remaining: Money, val estimatedMonth: YearMonth) : AffordabilityOutcome
}

/** [allocatedRatio] is clamped to 0..1 and is for UI progress only. */
data class BudgetTotals(
    val total: Money,
    val difference: Money,
    val isOver: Boolean,
    val allocatedRatio: Float,
)
