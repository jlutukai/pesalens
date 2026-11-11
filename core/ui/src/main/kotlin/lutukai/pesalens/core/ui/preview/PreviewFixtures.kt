package lutukai.pesalens.core.ui.preview

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.YearMonth
import kotlinx.datetime.plus
import lutukai.pesalens.core.model.AffordabilityOutcome
import lutukai.pesalens.core.model.BudgetTotals
import lutukai.pesalens.core.model.Category
import lutukai.pesalens.core.model.CategorySpend
import lutukai.pesalens.core.model.Money
import lutukai.pesalens.core.ui.catalog.model.AffordabilityFormUi
import lutukai.pesalens.core.ui.catalog.model.AffordabilityUi
import lutukai.pesalens.core.ui.catalog.model.BudgetLineUi
import lutukai.pesalens.core.ui.catalog.model.BudgetStatus
import lutukai.pesalens.core.ui.catalog.model.BudgetUi
import lutukai.pesalens.core.ui.catalog.model.CategorySpendUi
import lutukai.pesalens.core.ui.catalog.model.SpendingUi
import lutukai.pesalens.core.ui.catalog.model.TransactionUi
import lutukai.pesalens.core.ui.catalog.model.TxIcon
import lutukai.pesalens.core.ui.catalog.model.monthBlocks

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

    /** Prototype `AFFORD0()`: price 90,000, 30,000 set aside, 10,000 a month. */
    fun affordability(priceShillings: Long = 90_000, availableShillings: Long = 30_000, monthlyShillings: Long = 10_000): AffordabilityUi {
        val remaining = maxOf(priceShillings - availableShillings, 0L)
        val outcome = when {
            remaining == 0L -> AffordabilityOutcome.Covered
            monthlyShillings <= 0L -> AffordabilityOutcome.NoEndDate(Money.kes(remaining))
            else -> {
                val months = ((remaining + monthlyShillings - 1) / monthlyShillings).toInt()
                AffordabilityOutcome.OnTrack(months, Money.kes(remaining), month.plus(months, DateTimeUnit.MONTH))
            }
        }
        val percent = if (priceShillings > 0) (minOf(availableShillings, priceShillings) * 100 + priceShillings / 2) / priceShillings else 0L
        return AffordabilityUi(
            price = Money.kes(priceShillings),
            available = Money.kes(availableShillings),
            monthly = Money.kes(monthlyShillings),
            outcome = outcome,
            percentSetAside = percent.toInt(),
            monthBlocks = monthBlocks(month, (outcome as? AffordabilityOutcome.OnTrack)?.months ?: 0),
        )
    }

    fun affordabilityForm(
        available: String = "",
        monthly: String = "",
        tried: Boolean = false,
        submitted: Boolean = false,
    ) = AffordabilityFormUi(
        price = Money.kes(90_000),
        available = available,
        monthly = monthly,
        availableMissing = tried && available.isEmpty() && !submitted,
        monthlyMissing = tried && monthly.isEmpty() && !submitted,
        submitted = submitted,
    )

    /** Prototype `BUDGET0()`: income 100,000 and six limits totalling 75,000 (unallocated 25,000). */
    fun budget(
        incomeShillings: Long = 100_000,
        status: BudgetStatus = BudgetStatus.Draft,
        limitsShillings: List<Long> = listOf(25_000, 15_000, 10_000, 7_000, 8_000, 10_000),
    ): BudgetUi {
        val lines = novemberCategories.mapIndexed { i, c ->
            BudgetLineUi(c.category.id, c.category.label, c.category.tone, Money.kes(limitsShillings[i]))
        }
        val total = limitsShillings.sum()
        val difference = incomeShillings - total
        return BudgetUi(
            month = month,
            income = Money.kes(incomeShillings),
            lines = lines.toImmutableList(),
            totals = BudgetTotals(
                total = Money.kes(total),
                difference = Money.kes(difference),
                isOver = difference < 0,
                allocatedRatio = if (incomeShillings > 0) minOf(total.toFloat() / incomeShillings, 1f) else 1f,
            ),
            status = status,
        )
    }

    /** The same six categories with limits 10,000 over a 100,000 income. */
    fun overBudget(status: BudgetStatus = BudgetStatus.Draft): BudgetUi =
        budget(status = status, limitsShillings = listOf(30_000, 20_000, 10_000, 15_000, 20_000, 15_000))
}
