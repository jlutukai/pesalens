package lutukai.pesalens.core.data

import kotlinx.datetime.YearMonth
import lutukai.pesalens.core.common.AppError
import lutukai.pesalens.core.common.AppResult
import lutukai.pesalens.core.domain.repository.FinanceRepository
import lutukai.pesalens.core.model.BudgetDraft
import lutukai.pesalens.core.model.CategoryTransactions
import lutukai.pesalens.core.model.SpendingSummary

/** No financial data source is connected yet, so every call reports [AppError.NotConnected]. */
class NotConnectedFinanceRepository : FinanceRepository {
    private val notConnected = AppResult.Failure(AppError.NotConnected)

    override suspend fun spending(month: YearMonth): AppResult<SpendingSummary> = notConnected

    override suspend fun categoryTransactions(categoryId: String, month: YearMonth): AppResult<CategoryTransactions> =
        notConnected

    override suspend fun budgetDraft(month: YearMonth): AppResult<BudgetDraft> = notConnected

    override suspend fun saveBudget(draft: BudgetDraft): AppResult<Unit> = notConnected

    override suspend fun resetSampleData(): AppResult<Unit> = notConnected
}
