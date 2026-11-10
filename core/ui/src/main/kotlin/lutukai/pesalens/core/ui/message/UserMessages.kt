package lutukai.pesalens.core.ui.message

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import lutukai.pesalens.core.ui.text.UiText

fun interface UserMessageSink {
    fun show(message: UiText)
}

/** App-wide queue of transient user messages (snackbars). Messages are buffered until collected. */
class UserMessageQueue : UserMessageSink {
    private val channel = Channel<UiText>(Channel.BUFFERED)
    val messages: Flow<UiText> = channel.receiveAsFlow()

    override fun show(message: UiText) {
        channel.trySend(message)
    }
}
