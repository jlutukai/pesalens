package lutukai.pesalens.core.navigation

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavKey

/** Back stack operations for features. The app shell owns the back stack and provides [LocalNavigator]. */
interface Navigator {
    /** Pushes [destination] on top of the back stack. A no-op when the top is already equal to [destination]. */
    fun navigate(destination: PesaDestination)

    /** Pops the top destination. The last remaining destination is never popped. */
    fun back()

    /** Clears the back stack, leaving only [destination]. */
    fun replaceAll(destination: PesaDestination)

    /** Swaps the top destination for [destination]. */
    fun replaceTop(destination: PesaDestination)

    /** The top destination, or null when the top key is not a [PesaDestination]. */
    val current: PesaDestination?
}

/** [Navigator] over a Navigation 3 back stack, such as the one `rememberNavBackStack` returns. */
class BackStackNavigator(private val backStack: MutableList<NavKey>) : Navigator {

    override fun navigate(destination: PesaDestination) {
        if (backStack.lastOrNull() == destination) return
        backStack.add(destination)
    }

    override fun back() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    override fun replaceAll(destination: PesaDestination) {
        backStack.clear()
        backStack.add(destination)
    }

    override fun replaceTop(destination: PesaDestination) {
        if (backStack.isEmpty()) backStack.add(destination) else backStack[backStack.lastIndex] = destination
    }

    override val current: PesaDestination?
        get() = backStack.lastOrNull() as? PesaDestination
}

val LocalNavigator: ProvidableCompositionLocal<Navigator> =
    staticCompositionLocalOf { error("No Navigator provided. The app shell provides LocalNavigator around NavDisplay.") }
