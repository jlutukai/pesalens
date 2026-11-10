package lutukai.pesalens.core.testing.fake

import lutukai.pesalens.core.ui.message.UserMessageSink
import lutukai.pesalens.core.ui.text.UiText

class FakeUserMessageSink : UserMessageSink {
    private val _shown = mutableListOf<UiText>()
    val shown: List<UiText> get() = _shown

    override fun show(message: UiText) {
        _shown += message
    }
}
