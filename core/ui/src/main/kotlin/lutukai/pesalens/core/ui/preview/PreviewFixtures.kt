package lutukai.pesalens.core.ui.preview

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.YearMonth
import lutukai.pesalens.core.model.Category
import lutukai.pesalens.core.model.CategorySpend
import lutukai.pesalens.core.model.Money
import lutukai.pesalens.core.ui.catalog.model.CategorySpendUi
import lutukai.pesalens.core.ui.catalog.model.SpendingUi
import lutukai.pesalens.core.ui.catalog.model.TransactionUi
import lutukai.pesalens.core.ui.catalog.model.TxIcon

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
        // Half-up, like the prototype's Math.round.
        sharePercent = ((shillings * 100 + NOVEMBER_TOTAL_SHILLINGS / 2) / NOVEMBER_TOTAL_SHILLINGS).toInt(),
    )

    val novemberSpending: SpendingUi = SpendingUi(
        month = month,
        rangeLabel = "1–9 Nov",
        total = novemberTotal,
        categories = novemberCategories
            .map { CategorySpendUi(it.category.id, it.category.label, it.amount, it.sharePercent, it.category.tone) }
            .toImmutableList(),
        insight = "Rent and food make up 62% of this month's spending. " +
            "Food and groceries is the largest flexible category at KSh 15,000.",
    )

    /** Prototype `CATS[].tx`, keyed by category id. */
    val novemberTransactions: Map<String, ImmutableList<TransactionUi>> = mapOf(
        "rent" to txs("rent", tx("Monthly rent", "1 Nov", 25_000, TxIcon.House)),
        "food" to txs(
            "food",
            tx("Supermarket groceries", "2 Nov", 6_000, TxIcon.ShoppingCart),
            tx("Market shopping", "5 Nov", 3_500, TxIcon.Basket),
            tx("Lunches", "3–9 Nov · 6 entries", 3_000, TxIcon.ForkKnife),
            tx("Household groceries", "8 Nov", 2_500, TxIcon.ShoppingBag),
        ),
        "transport" to txs(
            "transport",
            tx("Fuel", "4 Nov", 4_000, TxIcon.GasPump),
            tx("Matatu fares", "1–9 Nov · 9 entries", 3_200, TxIcon.Bus),
            tx("Ride-hail trips", "6 Nov", 800, TxIcon.Car),
        ),
        "utilities" to txs(
            "utilities",
            tx("Electricity tokens", "2 Nov", 2_500, TxIcon.Lightning),
            tx("Home internet", "3 Nov", 2_000, TxIcon.WifiHigh),
            tx("Water bill", "7 Nov", 500, TxIcon.Drop),
        ),
        "shopping" to txs(
            "shopping",
            tx("Shoes", "6 Nov", 4_500, TxIcon.Sneaker),
            tx("Kitchen items", "8 Nov", 2_500, TxIcon.CookingPot),
        ),
        "other" to txs(
            "other",
            tx("Gym membership", "1 Nov", 2_500, TxIcon.Barbell),
            tx("Airtime and data", "4 Nov", 1_500, TxIcon.DeviceMobile),
            tx("Haircut", "7 Nov", 1_000, TxIcon.Scissors),
        ),
    )

    val foodTransactions: ImmutableList<TransactionUi> get() = novemberTransactions.getValue("food")

    private class TxSeed(val name: String, val date: String, val shillings: Long, val icon: TxIcon)

    private fun tx(name: String, date: String, shillings: Long, icon: TxIcon) = TxSeed(name, date, shillings, icon)

    private fun txs(categoryId: String, vararg seeds: TxSeed): ImmutableList<TransactionUi> = seeds
        .mapIndexed { i, t -> TransactionUi("$categoryId-$i", t.name, t.date, Money.kes(t.shillings), t.icon) }
        .toImmutableList()

    private const val NOVEMBER_TOTAL_SHILLINGS = 65_000L
}
