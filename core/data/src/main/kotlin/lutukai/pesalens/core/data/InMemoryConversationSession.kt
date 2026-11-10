package lutukai.pesalens.core.data

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import lutukai.pesalens.core.domain.repository.ConversationSession

/** Broadcasts "conversation cleared" to whoever is collecting; a clear with no collector is dropped. */
class InMemoryConversationSession : ConversationSession {
    private val events = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override val clears: Flow<Unit> = events

    override suspend fun clear() {
        events.emit(Unit)
    }
}
