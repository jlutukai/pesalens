package lutukai.pesalens.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private data class Screen(val name: String) : PesaDestination

private data object ForeignKey : NavKey

class BackStackNavigatorTest {

    private val home = Screen("home")
    private val spending = Screen("spending")
    private val settings = Screen("settings")

    private val backStack = mutableListOf<NavKey>(home)
    private val navigator = BackStackNavigator(backStack)

    @Test
    fun navigate_appendsDestination() {
        navigator.navigate(spending)

        assertEquals(listOf<NavKey>(home, spending), backStack)
        assertEquals(spending, navigator.current)
    }

    @Test
    fun navigate_sameAsTop_isIgnored() {
        navigator.navigate(spending)
        navigator.navigate(Screen("spending"))

        assertEquals(listOf<NavKey>(home, spending), backStack)
    }

    @Test
    fun navigate_differentFromTop_pushes() {
        navigator.navigate(spending)
        navigator.navigate(settings)
        navigator.navigate(spending)

        assertEquals(listOf<NavKey>(home, spending, settings, spending), backStack)
    }

    @Test
    fun back_popsTop_butNeverBelowOneEntry() {
        navigator.navigate(spending)

        navigator.back()
        assertEquals(listOf<NavKey>(home), backStack)

        navigator.back()
        assertEquals(listOf<NavKey>(home), backStack)
    }

    @Test
    fun replaceAll_leavesExactlyTheDestination() {
        navigator.navigate(spending)
        navigator.navigate(settings)

        navigator.replaceAll(spending)

        assertEquals(listOf<NavKey>(spending), backStack)
    }

    @Test
    fun replaceTop_swapsTheLastElement() {
        navigator.navigate(spending)

        navigator.replaceTop(settings)

        assertEquals(listOf<NavKey>(home, settings), backStack)
        assertEquals(settings, navigator.current)
    }

    @Test
    fun current_isNull_whenTopIsNotAPesaDestination() {
        backStack.add(ForeignKey)

        assertNull(navigator.current)
    }
}
