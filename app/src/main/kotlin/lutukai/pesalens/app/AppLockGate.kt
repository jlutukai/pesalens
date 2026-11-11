package lutukai.pesalens.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import lutukai.pesalens.core.navigation.Navigator
import lutukai.pesalens.feature.applock.api.Locked
import org.koin.compose.koinInject

/**
 * Shows [Locked] whenever [session] owes a lock, then clears the debt. Because the debt outlives the activity, a gate
 * in an activity recreated while the app was in the background still locks. Locked is never pushed onto Locked.
 *
 * It acts only while the activity is started, from the moment it starts: Locked then joins a back stack that is saved
 * with it, and a debt is never consumed into the state of an activity that may be destroyed before it shows again.
 */
@Composable
fun AppLockGate(navigator: Navigator, session: AppLockSession = koinInject()) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(navigator, session, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            session.lockOwed.collect { owed ->
                if (owed) {
                    if (navigator.current !is Locked) navigator.navigate(Locked)
                    session.consume()
                }
            }
        }
    }
}
