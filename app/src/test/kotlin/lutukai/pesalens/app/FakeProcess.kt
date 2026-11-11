package lutukai.pesalens.app

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

/** A process lifecycle the test drives by setting [registry]'s state, e.g. RESUMED → CREATED for ON_STOP. */
class FakeProcess : LifecycleOwner {
    val registry: LifecycleRegistry = LifecycleRegistry.createUnsafe(this)
    override val lifecycle: Lifecycle get() = registry
}
