package lutukai.pesalens.core.ui.preview

import kotlinx.datetime.YearMonth
import lutukai.pesalens.core.model.Category
import lutukai.pesalens.core.model.CategorySpend
import lutukai.pesalens.core.model.Money

/** Sample data for `@Preview`s only (November 2026, from the prototype). Never reference from runtime code. */
object PreviewFixtures {
    val month: YearMonth = YearMonth(2026, 11)

    val novemberCategories: List<CategorySpend> = listOf(
        spend("rent", "Rent", 0, 25_000),
        spend("food", "Food and groceries", 1, 15_000),
        spend("transport", "Transport", 2, 8_000),
        spend("utilities", "Utilities", 3, 5_000),
        spend("shopping", "Shopping", 4, 7_000),
        spend("other", "Other", 5, 5_000),
    )

    val novemberTotal: Money = novemberCategories.fold(Money.Zero) { acc, c -> acc + c.amount }

    private fun spend(id: String, label: String, tone: Int, shillings: Long) = CategorySpend(
        category = Category(id, label, tone),
        amount = Money.kes(shillings),
        sharePercent = (shillings * 100 / 65_000).toInt(),
    )
}
