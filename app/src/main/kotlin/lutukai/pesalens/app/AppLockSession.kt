package lutukai.pesalens.app

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import lutukai.pesalens.core.domain.repository.AppLockRepository
import lutukai.pesalens.core.model.LockMethod

/**
 * Process-wide record that a lock is owed: the app went to the background (process ON_STOP) with a lock method set.
 * It lives as long as the process, not an activity, so an activity destroyed and recreated while the app was away
 * still locks. [AppLockGate] shows Locked and then calls [consume]. A cold start owes nothing.
 */
class AppLockSession(
    private val lock: AppLockRepository,
    private val scope: CoroutineScope,
) {
    private val owed = MutableStateFlow(false)
    val lockOwed: StateFlow<Boolean> = owed.asStateFlow()

    private val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_STOP) {
            // The method in force as the app leaves decides it, read now rather than from a cached value.
            scope.launch {
                if (lock.state.first().method != LockMethod.None) owed.value = true
            }
        }
    }

    /** Starts watching [processLifecycle], `ProcessLifecycleOwner`'s in the app. Called once, by the application. */
    fun start(processLifecycle: Lifecycle) {
        processLifecycle.addObserver(observer)
    }

    /** Clears the debt once Locked is showing. */
    fun consume() {
        owed.value = false
    }
}
