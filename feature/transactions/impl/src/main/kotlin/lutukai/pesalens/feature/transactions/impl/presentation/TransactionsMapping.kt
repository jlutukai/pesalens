package lutukai.pesalens.feature.transactions.impl.presentation

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import lutukai.pesalens.core.model.CategoryTransactions
import lutukai.pesalens.core.model.Transaction
import lutukai.pesalens.core.ui.catalog.model.TransactionUi
import lutukai.pesalens.core.ui.catalog.model.TxIcon
import lutukai.pesalens.core.ui.format.formatDayMonth
import lutukai.pesalens.core.ui.text.UiText
import lutukai.pesalens.feature.transactions.impl.R

internal fun CategoryTransactions.toContent(): TransactionsUiState.Content = TransactionsUiState.Content(
    label = category.label,
    tone = category.tone,
    month = month,
    rangeLabel = "1–${coveredThrough.formatDayMonth()}",
    total = total,
    meta = UiText.Plural(R.plurals.transactions_meta, transactions.size, persistentListOf(transactions.size, sharePercent)),
    transactions = transactions.map { it.toTransactionUi() }.toImmutableList(),
)

internal fun Transaction.toTransactionUi(): TransactionUi = TransactionUi(
    id = id,
    name = merchant,
    dateLabel = dateLabel(),
    amount = amount,
    icon = merchant.txIcon(),
)

/** "2 Nov", or for an aggregate "3–9 Nov · 6 entries" ("30 Oct–2 Nov · 4 entries" across a month end). */
private fun Transaction.dateLabel(): String {
    val end = dateRangeEnd
    if (entryCount <= 1 || end == null) return date.formatDayMonth()
    val range = if (end.month == date.month) "${date.day}–${end.formatDayMonth()}" else "${date.formatDayMonth()}–${end.formatDayMonth()}"
    return "$range · $entryCount entries"
}

/** Phase 1 maps the fixture merchants by name; anything else gets the generic icon. */
private fun String.txIcon(): TxIcon = when (this) {
    "Monthly rent" -> TxIcon.House
    "Supermarket groceries" -> TxIcon.ShoppingCart
    "Market shopping" -> TxIcon.Basket
    "Lunches" -> TxIcon.ForkKnife
    "Household groceries" -> TxIcon.ShoppingBag
    "Fuel" -> TxIcon.GasPump
    "Matatu fares" -> TxIcon.Bus
    "Ride-hail trips" -> TxIcon.Car
    "Electricity tokens" -> TxIcon.Lightning
    "Home internet" -> TxIcon.WifiHigh
    "Water bill" -> TxIcon.Drop
    "Shoes" -> TxIcon.Sneaker
    "Kitchen items" -> TxIcon.CookingPot
    "Gym membership" -> TxIcon.Barbell
    "Airtime and data" -> TxIcon.DeviceMobile
    "Haircut" -> TxIcon.Scissors
    else -> TxIcon.Other
}
