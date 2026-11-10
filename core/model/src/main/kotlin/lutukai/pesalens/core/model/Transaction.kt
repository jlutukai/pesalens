package lutukai.pesalens.core.model

import kotlinx.datetime.LocalDate

enum class TransactionType { INCOME, EXPENSE }

/** Aggregates (e.g. "3-9 Nov, 6 entries") use [entryCount] and [dateRangeEnd]. */
data class Transaction(
    val id: String,
    val date: LocalDate,
    val amount: Money,
    val type: TransactionType,
    val merchant: String,
    val categoryId: String,
    val note: String?,
    val entryCount: Int = 1,
    val dateRangeEnd: LocalDate? = null,
)
