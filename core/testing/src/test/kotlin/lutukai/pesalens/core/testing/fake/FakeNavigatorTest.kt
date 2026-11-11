package lutukai.pesalens.core.testing.fake

import androidx.navigation3.runtime.NavKey
import lutukai.pesalens.core.navigation.BackStackNavigator
import lutukai.pesalens.core.navigation.Navigator
import lutukai.pesalens.core.navigation.PesaDestination
import kotlin.test.Test
import kotlin.test.assertEquals

private data class Screen(val name: String) : PesaDestination

/** FakeNavigator must behave like the real BackStackNavigator, or feature tests would pass against the wrong rules. */
class FakeNavigatorTest {

    private val home = Screen("home")
    private val settings = Screen("settings")
    private val model = Screen("model")
    private val lock = Screen("lock")

    private val scenarios: List<Pair<String, Navigator.() -> Unit>> = listOf(
        "push" to { navigate(settings); navigate(model) },
        "navigate same as top" to { navigate(settings); navigate(settings) },
        "navigate existing pops back to it" to { navigate(settings); navigate(model); navigate(settings) },
        "navigate root pops everything" to { navigate(settings); navigate(model); navigate(home) },
        "remove inclusive" to { navigate(settings); navigate(model); remove(settings) },
        "remove top" to { navigate(settings); navigate(model); remove(model) },
        "remove missing" to { navigate(settings); remove(lock) },
        "remove root never empties" to { navigate(settings); remove(home) },
        "remove twice" to { navigate(settings); navigate(model); remove(model); remove(model) },
        "back never empties" to { back(); back() },
        "replaceTop" to { navigate(settings); navigate(model); replaceTop(lock) },
        "replaceAll" to { navigate(settings); replaceAll(lock) },
    )

    @Test
    fun matchesBackStackNavigator() {
        scenarios.forEach { (name, script) ->
            val real = mutableListOf<NavKey>(home)
            BackStackNavigator(real).script()
            val fake = FakeNavigator(home).apply(script)

            assertEquals<List<NavKey>>(real, fake.stack, name)
            assertEquals<NavKey?>(real.last(), fake.current, name)
        }
    }

    @Test
    fun back_countsEveryCall() {
        val fake = FakeNavigator(home)
        fake.back()
        fake.back()
        assertEquals(2, fake.backCalls)
        assertEquals(listOf(home), fake.stack)
    }
}
