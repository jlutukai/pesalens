package lutukai.pesalens.core.navigation

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategyScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class AdaptiveOverlaySceneStrategyTest {

    private val home = entry("home")
    private val spending = entry("spending")
    private val consent = entry("consent", PesaPanes.overlay())

    @Test
    fun returnsNull_whenTopEntryHasNoOverlayMetadata() {
        val entries = listOf(home, consent, spending)

        assertNull(AdaptiveOverlaySceneStrategy<String>(useSheet = true).sceneFor(entries))
        assertNull(AdaptiveOverlaySceneStrategy<String>(useSheet = false).sceneFor(entries))
    }

    @Test
    fun overlayOnTop_returnsOverlaySceneOverTheEntriesBelow() {
        val scene = AdaptiveOverlaySceneStrategy<String>(useSheet = true).sceneFor(listOf(home, spending, consent))

        val overlay = assertIs<OverlayScene<String>>(scene)
        assertEquals(listOf(consent), overlay.entries)
        assertEquals(listOf(home, spending), overlay.overlaidEntries)
        assertEquals(listOf(home, spending), overlay.previousEntries)
    }

    @Test
    fun dialogMode_alsoReturnsOverlayScene() {
        val scene = AdaptiveOverlaySceneStrategy<String>(useSheet = false).sceneFor(listOf(home, consent))

        val overlay = assertIs<OverlayScene<String>>(scene)
        assertEquals(listOf(consent), overlay.entries)
        assertEquals(listOf(home), overlay.overlaidEntries)
    }

    @Test
    fun scene_isStableAcrossCalculations_andKeyedPerMode() {
        val entries = listOf(home, consent)
        val sheet = AdaptiveOverlaySceneStrategy<String>(useSheet = true)

        val first = sheet.sceneFor(entries)
        val second = sheet.sceneFor(entries)
        val dialog = AdaptiveOverlaySceneStrategy<String>(useSheet = false).sceneFor(entries)

        assertEquals(first, second)
        assertEquals(first?.key, second?.key)
        assertNotEquals(first?.key, dialog?.key)
    }

    @Test
    fun returnsNull_whenOverlayHasNothingBeneathIt() {
        assertNull(AdaptiveOverlaySceneStrategy<String>(useSheet = true).sceneFor(listOf(consent)))
    }

    private fun entry(key: String, metadata: Map<String, Any> = emptyMap()) =
        NavEntry(key, metadata = metadata) { }

    private fun AdaptiveOverlaySceneStrategy<String>.sceneFor(entries: List<NavEntry<String>>): Scene<String>? =
        with(this) { SceneStrategyScope<String>().calculateScene(entries) }
}
