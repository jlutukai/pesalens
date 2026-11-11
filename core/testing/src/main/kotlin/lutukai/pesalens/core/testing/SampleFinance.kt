package lutukai.pesalens.core.testing

import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import lutukai.pesalens.core.model.BudgetDraft
import lutukai.pesalens.core.model.BudgetLine
import lutukai.pesalens.core.model.Category
import lutukai.pesalens.core.model.CategorySpend
import lutukai.pesalens.core.model.CategoryTransactions
import lutukai.pesalens.core.model.Money
import lutukai.pesalens.core.model.SpendingSummary
import lutukai.pesalens.core.model.Transaction
import lutukai.pesalens.core.model.TransactionType

/** Values from the prototype's `CATS` and `BUDGET0`. For tests only; the running app has no sample data. */
object SampleFinance {
    private val month = YearMonth(2026, 11)
    private val coveredThrough = LocalDate(2026, 11, 9)

    private val rent = Category("rent", "Rent", 0)
    private val food = Category("food", "Food and groceries", 1)
    private val transport = Category("transport", "Transport", 2)
    private val utilities = Category("utilities", "Utilities", 3)
    private val shopping = Category("shopping", "Shopping", 4)
    private val other = Category("other", "Other", 5)

    private fun tx(
        id: String,
        category: Category,
        merchant: String,
        day: Int,
        shillings: Long,
        entryCount: Int = 1,
        endDay: Int? = null,
    ) = Transaction(
        id = id,
        date = LocalDate(2026, 11, day),
        amount = Money.kes(shillings),
        type = TransactionType.EXPENSE,
        merchant = merchant,
        categoryId = category.id,
        note = null,
        entryCount = entryCount,
        dateRangeEnd = endDay?.let { LocalDate(2026, 11, it) },
    )

    private val transactions: Map<Category, List<Transaction>> = mapOf(
        rent to listOf(tx("rent-1", rent, "Monthly rent", 1, 25_000)),
        food to listOf(
            tx("food-1", food, "Supermarket groceries", 2, 6_000),
            tx("food-2", food, "Market shopping", 5, 3_500),
            tx("food-3", food, "Lunches", 3, 3_000, entryCount = 6, endDay = 9),
            tx("food-4", food, "Household groceries", 8, 2_500),
        ),
        transport to listOf(
            tx("transport-1", transport, "Fuel", 4, 4_000),
            tx("transport-2", transport, "Matatu fares", 1, 3_200, entryCount = 9, endDay = 9),
            tx("transport-3", transport, "Ride-hail trips", 6, 800),
        ),
        utilities to listOf(
            tx("utilities-1", utilities, "Electricity tokens", 2, 2_500),
            tx("utilities-2", utilities, "Home internet", 3, 2_000),
            tx("utilities-3", utilities, "Water bill", 7, 500),
        ),
        shopping to listOf(
            tx("shopping-1", shopping, "Shoes", 6, 4_500),
            tx("shopping-2", shopping, "Kitchen items", 8, 2_500),
        ),
        other to listOf(
            tx("other-1", other, "Gym membership", 1, 2_500),
            tx("other-2", other, "Airtime and data", 4, 1_500),
            tx("other-3", other, "Haircut", 7, 1_000),
        ),
    )

    private fun List<Transaction>.sum(): Money = fold(Money.Zero) { acc, t -> acc + t.amount }

    private val grandTotal: Money = transactions.values.flatten().sum()

    /** Rounds half-up like the prototype's `Math.round`, in integer arithmetic. */
    private fun share(amount: Money): Int = ((amount.minor * 100 * 2 + grandTotal.minor) / (grandTotal.minor * 2)).toInt()

    val november: SpendingSummary = SpendingSummary(
        month = month,
        coveredThrough = coveredThrough,
        total = grandTotal,
        categories = transactions.map { (category, list) ->
            val amount = list.sum()
            CategorySpend(category, amount, share(amount))
        },
        focusCategoryId = "food",
    )

    fun categoryTransactions(id: String): CategoryTransactions {
        val category = transactions.keys.first { it.id == id }
        val list = transactions.getValue(category)
        val total = list.sum()
        return CategoryTransactions(category, month, coveredThrough, total, share(total), list)
    }

    val draft: BudgetDraft = BudgetDraft(
        month = month,
        income = Money.kes(100_000),
        lines = listOf(25_000L, 15_000L, 10_000L, 7_000L, 8_000L, 10_000L)
            .mapIndexed { i, limit -> BudgetLine(transactions.keys.elementAt(i), Money.kes(limit)) },
    )
}
