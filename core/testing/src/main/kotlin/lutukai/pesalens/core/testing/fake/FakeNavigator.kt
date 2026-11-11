package lutukai.pesalens.core.testing.fake

import lutukai.pesalens.core.navigation.Navigator
import lutukai.pesalens.core.navigation.PesaDestination

/**
 * [Navigator] over a plain list, with the same semantics as `BackStackNavigator`. [back] counts every call but, like
 * the real one, never pops the last entry.
 */
class FakeNavigator(start: PesaDestination? = null) : Navigator {
    private val entries = listOfNotNull(start).toMutableList()

    val stack: List<PesaDestination> get() = entries

    var backCalls: Int = 0
        private set

    override fun navigate(destination: PesaDestination) {
        val index = entries.lastIndexOf(destination)
        if (index >= 0) popAbove(index) else entries.add(destination)
    }

    override fun back() {
        backCalls++
        if (entries.size > 1) entries.removeAt(entries.lastIndex)
    }

    override fun remove(destination: PesaDestination) {
        val index = entries.lastIndexOf(destination)
        if (index >= 0) popAbove(maxOf(index - 1, 0))
    }

    private fun popAbove(index: Int) {
        while (entries.lastIndex > index) entries.removeAt(entries.lastIndex)
    }

    override fun replaceAll(destination: PesaDestination) {
        entries.clear()
        entries.add(destination)
    }

    override fun replaceTop(destination: PesaDestination) {
        if (entries.isEmpty()) entries.add(destination) else entries[entries.lastIndex] = destination
    }

    override val current: PesaDestination? get() = entries.lastOrNull()
}
