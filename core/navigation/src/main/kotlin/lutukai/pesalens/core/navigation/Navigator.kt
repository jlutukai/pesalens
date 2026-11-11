package lutukai.pesalens.core.navigation

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavKey

/** Back stack operations for features. The app shell owns the back stack and provides [LocalNavigator]. */
interface Navigator {
    /**
     * Shows [destination]. Destinations are single-instance: when [destination] is already on the back stack, the
     * entries above it are popped instead of pushing a second copy (so a no-op when it is already on top).
     */
    fun navigate(destination: PesaDestination)

    /** Pops the top destination. The last remaining destination is never popped. */
    fun back()

    /**
     * Removes [destination] and everything above it (pop up to it, inclusive). A screen that closes itself uses this
     * rather than [back], which pops whatever is on top: with panes side by side that can be a different pane. Nothing
     * happens when [destination] is not on the back stack, and the bottom entry is never removed.
     */
    fun remove(destination: PesaDestination)

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
        val index = backStack.lastIndexOf(destination)
        if (index >= 0) popAbove(index) else backStack.add(destination)
    }

    override fun back() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    override fun remove(destination: PesaDestination) {
        val index = backStack.lastIndexOf(destination)
        if (index >= 0) popAbove(maxOf(index - 1, 0))
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

    /** Pops every entry above [index]. */
    private fun popAbove(index: Int) {
        while (backStack.lastIndex > index) backStack.removeAt(backStack.lastIndex)
    }
}

val LocalNavigator: ProvidableCompositionLocal<Navigator> =
    staticCompositionLocalOf { error("No Navigator provided. The app shell provides LocalNavigator around NavDisplay.") }
