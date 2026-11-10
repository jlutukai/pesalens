package lutukai.pesalens.core.testing.fake

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import lutukai.pesalens.core.domain.repository.ConversationSession

class FakeConversationSession : ConversationSession {
    private val events = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override val clears: Flow<Unit> = events

    var clearCalls: Int = 0
        private set

    override suspend fun clear() {
        clearCalls++
        events.emit(Unit)
    }
}
