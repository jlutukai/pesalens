package lutukai.pesalens.core.data

import lutukai.pesalens.core.domain.ai.InterpretFailure
import lutukai.pesalens.core.domain.ai.IntentInterpreter
import lutukai.pesalens.core.domain.ai.Interpretation
import lutukai.pesalens.core.model.AssistantMode

/** No assistant backend is connected yet: cloud is unreachable and no on-device model exists. */
class NotConnectedIntentInterpreter : IntentInterpreter {
    override suspend fun interpret(text: String, mode: AssistantMode): Interpretation = Interpretation.Failed(
        when (mode) {
            AssistantMode.Cloud -> InterpretFailure.CloudUnreachable
            AssistantMode.OnDevice -> InterpretFailure.ModelUnavailable
        },
    )
}
