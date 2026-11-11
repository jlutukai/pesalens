package lutukai.pesalens.feature.transactions.impl.presentation

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.YearMonth
import lutukai.pesalens.core.common.AppError
import lutukai.pesalens.core.model.Money
import lutukai.pesalens.core.ui.catalog.model.TransactionUi
import lutukai.pesalens.core.ui.mvi.UiEffect
import lutukai.pesalens.core.ui.mvi.UiIntent
import lutukai.pesalens.core.ui.mvi.UiState
import lutukai.pesalens.core.ui.text.UiText

@Immutable
sealed interface TransactionsUiState : UiState {
    data object Loading : TransactionsUiState

    /**
     * [tone] selects the category's chart colour and pattern; [rangeLabel] is "1–9 Nov"; [meta] is
     * "4 sample transactions · 23% of spending" (a plural, so it resolves in the UI layer).
     */
    data class Content(
        val label: String,
        val tone: Int,
        val month: YearMonth,
        val rangeLabel: String,
        val total: Money,
        val meta: UiText,
        val transactions: ImmutableList<TransactionUi>,
    ) : TransactionsUiState

    data class Empty(val label: String, val month: YearMonth) : TransactionsUiState

    data class Error(val error: AppError) : TransactionsUiState
}

sealed interface TransactionsIntent : UiIntent {
    data class Load(val categoryId: String, val month: YearMonth) : TransactionsIntent
    data object Back : TransactionsIntent
    data object Retry : TransactionsIntent
}

sealed interface TransactionsEffect : UiEffect {
    data object Close : TransactionsEffect
}
