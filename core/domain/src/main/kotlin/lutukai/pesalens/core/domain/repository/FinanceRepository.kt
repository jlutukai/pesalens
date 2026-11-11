package lutukai.pesalens.core.domain.repository

import kotlinx.datetime.YearMonth
import lutukai.pesalens.core.common.AppResult
import lutukai.pesalens.core.model.BudgetDraft
import lutukai.pesalens.core.model.CategoryTransactions
import lutukai.pesalens.core.model.SpendingSummary

interface FinanceRepository {
    suspend fun spending(month: YearMonth): AppResult<SpendingSummary>

    suspend fun categoryTransactions(categoryId: String, month: YearMonth): AppResult<CategoryTransactions>

    suspend fun budgetDraft(month: YearMonth): AppResult<BudgetDraft>

    suspend fun saveBudget(draft: BudgetDraft): AppResult<Unit>

    suspend fun resetSampleData(): AppResult<Unit>
}
