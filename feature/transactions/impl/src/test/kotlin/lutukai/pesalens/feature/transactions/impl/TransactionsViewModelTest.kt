package lutukai.pesalens.feature.transactions.impl

import app.cash.turbine.test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import lutukai.pesalens.core.common.AppError
import lutukai.pesalens.core.common.AppResult
import lutukai.pesalens.core.model.Money
import lutukai.pesalens.core.model.Transaction
import lutukai.pesalens.core.model.TransactionType
import lutukai.pesalens.core.testing.MainDispatcherRule
import lutukai.pesalens.core.testing.SampleFinance
import lutukai.pesalens.core.testing.fake.FakeFinanceRepository
import lutukai.pesalens.core.ui.catalog.model.TxIcon
import lutukai.pesalens.core.ui.text.UiText
import lutukai.pesalens.feature.transactions.impl.presentation.TransactionsEffect
import lutukai.pesalens.feature.transactions.impl.presentation.TransactionsIntent
import lutukai.pesalens.feature.transactions.impl.presentation.TransactionsUiState
import lutukai.pesalens.feature.transactions.impl.presentation.TransactionsViewModel
import lutukai.pesalens.feature.transactions.impl.presentation.toTransactionUi
import org.junit.Rule
import org.junit.Test

class TransactionsViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()

    private val november = YearMonth(2026, 11)
    private val finance = FakeFinanceRepository()
    private val vm = TransactionsViewModel(finance)

    private fun txn(merchant: String, day: Int, entryCount: Int = 1, endDay: Int? = null) = Transaction(
        id = "t", date = LocalDate(2026, 11, day), amount = Money.kes(100), type = TransactionType.EXPENSE,
        merchant = merchant, categoryId = "food", note = null, entryCount = entryCount,
        dateRangeEnd = endDay?.let { LocalDate(2026, 11, it) },
    )

    @Test
    fun initialState_isLoading() {
        assertEquals(TransactionsUiState.Loading, vm.state.value)
    }

    @Test
    fun load_success_mapsContentWithMeta() = runTest {
        vm.onIntent(TransactionsIntent.Load("food", november))
        val content = assertIs<TransactionsUiState.Content>(vm.state.value)
        assertEquals("Food and groceries", content.label)
        assertEquals(1, content.tone)
        assertEquals(november, content.month)
        assertEquals("1–9 Nov", content.rangeLabel)
        assertEquals(Money.kes(15_000), content.total)
        assertEquals(UiText.Plural(R.plurals.transactions_meta, 4, kotlinx.collections.immutable.persistentListOf(4, 23)), content.meta)
        assertEquals(
            listOf("Supermarket groceries", "Market shopping", "Lunches", "Household groceries"),
            content.transactions.map { it.name },
        )
        assertEquals(listOf("2 Nov", "5 Nov", "3–9 Nov · 6 entries", "8 Nov"), content.transactions.map { it.dateLabel })
        assertEquals(
            listOf(TxIcon.ShoppingCart, TxIcon.Basket, TxIcon.ForkKnife, TxIcon.ShoppingBag),
            content.transactions.map { it.icon },
        )
    }

    @Test
    fun load_noTransactions_showsEmpty() = runTest {
        finance.transactionsResult = AppResult.Success(SampleFinance.categoryTransactions("food").copy(transactions = emptyList()))
        vm.onIntent(TransactionsIntent.Load("food", november))
        assertEquals(TransactionsUiState.Empty("Food and groceries", november), vm.state.value)
    }

    @Test
    fun load_notConnected_showsError() = runTest {
        finance.transactionsResult = AppResult.Failure(AppError.NotConnected)
        vm.onIntent(TransactionsIntent.Load("food", november))
        assertEquals(TransactionsUiState.Error(AppError.NotConnected), vm.state.value)
    }

    @Test
    fun load_sameArgsTwice_loadsOnce() = runTest {
        vm.onIntent(TransactionsIntent.Load("food", november))
        finance.transactionsResult = AppResult.Failure(AppError.Storage)
        vm.onIntent(TransactionsIntent.Load("food", november))
        assertIs<TransactionsUiState.Content>(vm.state.value)
    }

    @Test
    fun load_differentArgs_reloads() = runTest {
        vm.onIntent(TransactionsIntent.Load("food", november))
        finance.transactionsResult = AppResult.Failure(AppError.Storage)
        vm.onIntent(TransactionsIntent.Load("rent", november))
        assertEquals(TransactionsUiState.Error(AppError.Storage), vm.state.value)
    }

    @Test
    fun retry_reloads() = runTest {
        finance.transactionsResult = AppResult.Failure(AppError.NotConnected)
        vm.onIntent(TransactionsIntent.Load("food", november))
        assertIs<TransactionsUiState.Error>(vm.state.value)
        finance.transactionsResult = AppResult.Success(SampleFinance.categoryTransactions("food"))
        vm.onIntent(TransactionsIntent.Retry)
        assertIs<TransactionsUiState.Content>(vm.state.value)
    }

    @Test
    fun retry_beforeLoad_doesNothing() = runTest {
        vm.onIntent(TransactionsIntent.Retry)
        assertEquals(TransactionsUiState.Loading, vm.state.value)
    }

    @Test
    fun back_doubleTap_emitsCloseOnce() = runTest {
        vm.effects.test {
            vm.onIntent(TransactionsIntent.Back)
            vm.onIntent(TransactionsIntent.Back)
            assertEquals(TransactionsEffect.Close, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun aggregateDateLabel_format() {
        assertEquals("3–9 Nov · 6 entries", txn("Lunches", 3, entryCount = 6, endDay = 9).toTransactionUi().dateLabel)
        assertEquals("3 Nov", txn("Lunches", 3).toTransactionUi().dateLabel)
    }

    @Test
    fun aggregateDateLabel_acrossMonths_namesBoth() {
        val t = txn("Lunches", 30, entryCount = 4).copy(date = LocalDate(2026, 10, 30), dateRangeEnd = LocalDate(2026, 11, 2))
        assertEquals("30 Oct–2 Nov · 4 entries", t.toTransactionUi().dateLabel)
    }

    @Test
    fun unknownMerchant_usesOtherIcon() {
        assertEquals(TxIcon.Other, txn("Mystery", 3).toTransactionUi().icon)
    }
}
