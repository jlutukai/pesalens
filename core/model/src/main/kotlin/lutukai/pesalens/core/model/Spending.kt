package lutukai.pesalens.core.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

data class CategorySpend(val category: Category, val amount: Money, val sharePercent: Int)

data class SpendingSummary(
    val month: YearMonth,
    val coveredThrough: LocalDate,
    val total: Money,
    val categories: List<CategorySpend>,
    val focusCategoryId: String?,
)

data class CategoryTransactions(
    val category: Category,
    val month: YearMonth,
    val coveredThrough: LocalDate,
    val total: Money,
    val sharePercent: Int,
    val transactions: List<Transaction>,
)
