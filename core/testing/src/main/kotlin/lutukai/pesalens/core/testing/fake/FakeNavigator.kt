package lutukai.pesalens.core.testing.fake

import lutukai.pesalens.core.navigation.Navigator
import lutukai.pesalens.core.navigation.PesaDestination

/** [Navigator] over a plain list. [back] counts every call but, like the real one, never pops the last entry. */
class FakeNavigator(start: PesaDestination? = null) : Navigator {
    private val entries = listOfNotNull(start).toMutableList()

    val stack: List<PesaDestination> get() = entries

    var backCalls: Int = 0
        private set

    override fun navigate(destination: PesaDestination) {
        if (entries.lastOrNull() == destination) return
        entries.add(destination)
    }

    override fun back() {
        backCalls++
        if (entries.size > 1) entries.removeAt(entries.lastIndex)
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
