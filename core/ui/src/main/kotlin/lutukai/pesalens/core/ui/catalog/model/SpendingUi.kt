package lutukai.pesalens.core.ui.catalog.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.YearMonth
import lutukai.pesalens.core.model.Money
import lutukai.pesalens.core.ui.format.formatKes
import lutukai.pesalens.core.ui.format.formatLong

/** [tone] 0..5 selects the chart colour slot and pattern. */
@Immutable
data class CategorySpendUi(val id: String, val label: String, val amount: Money, val sharePercent: Int, val tone: Int)

@Immutable
data class SpendingUi(
    val month: YearMonth,
    /** "1–9 Nov". */
    val rangeLabel: String,
    val total: Money,
    val categories: ImmutableList<CategorySpendUi>,
    val insight: String?,
)

/**
 * Text alternative for the chart, e.g. "Spending for November 2026 totals KSh 65,000. Rent KSh 25,000 (38%), ….".
 * With [patternNames] (one per tone) each share also names its pattern: "(38%, solid)".
 *
 * Money and month formatting are fixed en-US/KES (see `formatKes`), so the sentence frame is kept here with them
 * instead of being split across string resources.
 */
fun SpendingUi.accessibilitySummary(patternNames: List<String>? = null): String {
    val parts = categories.joinToString(", ") { c ->
        val share = patternNames?.getOrNull(c.tone)?.let { "${c.sharePercent}%, $it" } ?: "${c.sharePercent}%"
        "${c.label} ${c.amount.formatKes()} ($share)"
    }
    return "Spending for ${month.formatLong()} totals ${total.formatKes()}. $parts."
}
