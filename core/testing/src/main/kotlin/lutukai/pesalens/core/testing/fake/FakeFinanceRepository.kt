package lutukai.pesalens.core.testing.fake

import kotlinx.datetime.YearMonth
import lutukai.pesalens.core.common.AppResult
import lutukai.pesalens.core.domain.repository.FinanceRepository
import lutukai.pesalens.core.model.BudgetDraft
import lutukai.pesalens.core.model.CategoryTransactions
import lutukai.pesalens.core.model.SpendingSummary
import lutukai.pesalens.core.testing.SampleFinance

class FakeFinanceRepository : FinanceRepository {
    var spendingResult: AppResult<SpendingSummary> = AppResult.Success(SampleFinance.november)
    var transactionsResult: AppResult<CategoryTransactions> = AppResult.Success(SampleFinance.categoryTransactions("food"))
    var draftResult: AppResult<BudgetDraft> = AppResult.Success(SampleFinance.draft)
    var saveResult: AppResult<Unit> = AppResult.Success(Unit)
    var resetResult: AppResult<Unit> = AppResult.Success(Unit)

    private val _savedDrafts = mutableListOf<BudgetDraft>()
    val savedDrafts: List<BudgetDraft> get() = _savedDrafts

    var resetCalls: Int = 0
        private set

    override suspend fun spending(month: YearMonth): AppResult<SpendingSummary> = spendingResult

    override suspend fun categoryTransactions(categoryId: String, month: YearMonth): AppResult<CategoryTransactions> =
        transactionsResult

    override suspend fun budgetDraft(month: YearMonth): AppResult<BudgetDraft> = draftResult

    override suspend fun saveBudget(draft: BudgetDraft): AppResult<Unit> {
        _savedDrafts += draft
        return saveResult
    }

    override suspend fun resetSampleData(): AppResult<Unit> {
        resetCalls++
        return resetResult
    }
}
