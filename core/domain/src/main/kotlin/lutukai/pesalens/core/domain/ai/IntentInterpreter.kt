package lutukai.pesalens.core.domain.ai

import kotlinx.datetime.YearMonth
import lutukai.pesalens.core.model.AssistantMode
import lutukai.pesalens.core.model.Money

sealed interface Interpretation {
    data class Spending(val month: YearMonth) : Interpretation

    data class CategoryDetail(val categoryId: String, val month: YearMonth) : Interpretation

    data class Affordability(val price: Money?) : Interpretation

    data object Budget : Interpretation

    data object Unsupported : Interpretation

    data class Failed(val reason: InterpretFailure) : Interpretation
}

enum class InterpretFailure { CloudUnreachable, ModelUnavailable, InvalidOutput }

interface IntentInterpreter {
    suspend fun interpret(text: String, mode: AssistantMode): Interpretation
}
