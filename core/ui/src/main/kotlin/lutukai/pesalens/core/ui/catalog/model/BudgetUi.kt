package lutukai.pesalens.core.ui.catalog.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.YearMonth
import lutukai.pesalens.core.model.BudgetTotals
import lutukai.pesalens.core.model.Money

/** [tone] 0..5 selects the chart colour slot and pattern of the line's swatch. */
@Immutable
data class BudgetLineUi(val categoryId: String, val label: String, val tone: Int, val limit: Money)

enum class BudgetStatus { Draft, Saved, Discarded }

@Immutable
data class BudgetUi(
    val month: YearMonth,
    val income: Money,
    val lines: ImmutableList<BudgetLineUi>,
    val totals: BudgetTotals,
    val status: BudgetStatus,
)

/** Digits-only whole shillings, the value format the amount fields use ("25000"). */
internal fun Money.shillingDigits(): String = wholeShillings.toString()
