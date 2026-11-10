package lutukai.pesalens.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import lutukai.pesalens.core.domain.repository.AppLockRepository
import lutukai.pesalens.core.model.LockMethod
import lutukai.pesalens.core.navigation.Navigator
import lutukai.pesalens.feature.applock.api.Locked
import org.koin.compose.koinInject

/**
 * Shows [Locked] when the app comes back from the background with a lock method set. Only an ON_START that follows an
 * ON_STOP of [processLifecycle] locks: phase 1's lock lasts for the session, so there is nothing to unlock at launch.
 * [Locked] is never pushed while it is already on top.
 */
@Composable
fun AppLockGate(
    navigator: Navigator,
    lock: AppLockRepository = koinInject(),
    processLifecycle: Lifecycle = ProcessLifecycleOwner.get().lifecycle,
) {
    val scope = rememberCoroutineScope()
    DisposableEffect(navigator, lock, processLifecycle) {
        var backgrounded = false
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                backgrounded = true
            } else if (event == Lifecycle.Event.ON_START && backgrounded) {
                backgrounded = false
                // Undispatched, so the check runs at this ON_START, ahead of anything else the return triggers (such
                // as an unlock result popping Locked). The method is read now, not cached: it may have changed.
                scope.launch(start = CoroutineStart.UNDISPATCHED) {
                    if (lock.state.first().method != LockMethod.None && navigator.current !is Locked) {
                        navigator.navigate(Locked)
                    }
                }
            }
        }
        processLifecycle.addObserver(observer)
        onDispose { processLifecycle.removeObserver(observer) }
    }
}
