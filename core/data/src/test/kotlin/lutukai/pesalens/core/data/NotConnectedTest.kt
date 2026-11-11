package lutukai.pesalens.core.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.YearMonth
import lutukai.pesalens.core.common.AppError
import lutukai.pesalens.core.common.AppResult
import lutukai.pesalens.core.domain.ai.InterpretFailure
import lutukai.pesalens.core.domain.ai.Interpretation
import lutukai.pesalens.core.model.AssistantMode
import lutukai.pesalens.core.model.BudgetDraft
import lutukai.pesalens.core.model.ModelStatus
import lutukai.pesalens.core.model.Money
import kotlin.test.Test
import kotlin.test.assertEquals

class NotConnectedTest {
    private val month = YearMonth(2026, 11)
    private val notConnected = AppResult.Failure(AppError.NotConnected)

    @Test
    fun interpreter_cloud_failsCloudUnreachable() = runTest {
        assertEquals(
            Interpretation.Failed(InterpretFailure.CloudUnreachable),
            NotConnectedIntentInterpreter().interpret("hi", AssistantMode.Cloud),
        )
    }

    @Test
    fun interpreter_onDevice_failsModelUnavailable() = runTest {
        assertEquals(
            Interpretation.Failed(InterpretFailure.ModelUnavailable),
            NotConnectedIntentInterpreter().interpret("hi", AssistantMode.OnDevice),
        )
    }

    @Test
    fun finance_everyCall_returnsNotConnected() = runTest {
        val repo = NotConnectedFinanceRepository()
        assertEquals(notConnected, repo.spending(month))
        assertEquals(notConnected, repo.categoryTransactions("food", month))
        assertEquals(notConnected, repo.budgetDraft(month))
        assertEquals(notConnected, repo.saveBudget(BudgetDraft(month, Money.kes(1), emptyList())))
        assertEquals(notConnected, repo.resetSampleData())
    }

    @Test
    fun modelManager_isAlwaysNotDownloaded() = runTest {
        val manager = NotConnectedModelManager()
        assertEquals(ModelStatus.NotDownloaded, manager.status.first())
        assertEquals(notConnected, manager.checkDevice())
        manager.startDownload()
        manager.cancelDownload()
        manager.delete()
        assertEquals(ModelStatus.NotDownloaded, manager.status.first())
    }
}
