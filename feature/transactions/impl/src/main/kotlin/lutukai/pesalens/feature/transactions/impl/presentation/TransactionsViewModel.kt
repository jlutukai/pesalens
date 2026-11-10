package lutukai.pesalens.feature.transactions.impl.presentation

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.datetime.YearMonth
import lutukai.pesalens.core.common.AppResult
import lutukai.pesalens.core.domain.repository.FinanceRepository
import lutukai.pesalens.core.ui.mvi.MviViewModel

class TransactionsViewModel(
    private val finance: FinanceRepository,
) : MviViewModel<TransactionsUiState, TransactionsIntent, TransactionsEffect>(TransactionsUiState.Loading) {

    private data class Request(val categoryId: String, val month: YearMonth)

    private var request: Request? = null
    private var loadJob: Job? = null
    private var closed = false

    override fun handleIntent(intent: TransactionsIntent) = when (intent) {
        is TransactionsIntent.Load -> load(Request(intent.categoryId, intent.month), force = false)
        TransactionsIntent.Retry -> request?.let { load(it, force = true) } ?: Unit
        TransactionsIntent.Back -> close()
    }

    /** The screen re-sends Load on recomposition and rotation; the same category and month is not reloaded. */
    private fun load(requested: Request, force: Boolean) {
        if (!force && requested == request) return
        request = requested
        loadJob?.cancel()
        reduce { TransactionsUiState.Loading }
        loadJob = viewModelScope.launch {
            val next = when (val result = finance.categoryTransactions(requested.categoryId, requested.month)) {
                is AppResult.Success -> {
                    val data = result.value
                    if (data.transactions.isEmpty()) {
                        TransactionsUiState.Empty(data.category.label, data.month)
                    } else {
                        data.toContent()
                    }
                }
                is AppResult.Failure -> TransactionsUiState.Error(result.error)
            }
            reduce { next }
        }
    }

    /** One navigation effect; further taps (double tap) are ignored. */
    private fun close() {
        if (closed) return
        closed = true
        emit(TransactionsEffect.Close)
    }
}
