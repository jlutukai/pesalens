package lutukai.pesalens.core.testing.fake

import lutukai.pesalens.core.domain.ai.IntentInterpreter
import lutukai.pesalens.core.domain.ai.Interpretation
import lutukai.pesalens.core.model.AssistantMode

class FakeIntentInterpreter : IntentInterpreter {
    var next: Interpretation = Interpretation.Unsupported

    private val _received = mutableListOf<Pair<String, AssistantMode>>()
    val received: List<Pair<String, AssistantMode>> get() = _received

    override suspend fun interpret(text: String, mode: AssistantMode): Interpretation {
        _received += text to mode
        return next
    }
}
