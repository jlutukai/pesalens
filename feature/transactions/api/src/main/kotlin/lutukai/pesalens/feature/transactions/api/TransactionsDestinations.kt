package lutukai.pesalens.feature.transactions.api

import kotlinx.serialization.Serializable
import lutukai.pesalens.core.navigation.PesaDestination

/** One category's transactions for a month; [month] is the 1-based month number. */
@Serializable
data class CategoryTransactions(val categoryId: String, val year: Int, val month: Int) : PesaDestination
