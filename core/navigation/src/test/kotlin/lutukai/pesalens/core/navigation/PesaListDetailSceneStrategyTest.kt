package lutukai.pesalens.core.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldDefaults
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategyScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class PesaListDetailSceneStrategyTest {

    private val welcome = NavEntry("welcome") { }
    private val assistant = NavEntry("assistant", metadata = PesaPanes.list(detailPlaceholder = {})) { }
    private val settings = NavEntry("settings", metadata = PesaPanes.detail()) { }
    private val modelSetup = NavEntry("modelSetup", metadata = PesaPanes.extra()) { }
    private val lockSetup = NavEntry("lockSetup", metadata = PesaPanes.extra()) { }

    @Test
    fun extraOnly_returnsNull_soItShowsAsASinglePane() {
        // Onboarding's lock setup: the only entry on the back stack.
        assertNull(sceneFor(listOf(lockSetup), partitions = 3))
        assertNull(sceneFor(listOf(lockSetup), partitions = 2))
    }

    @Test
    fun extraAboveANonPaneEntry_returnsNull() {
        // Model setup opened from Welcome, which is not a pane.
        assertNull(sceneFor(listOf(welcome, modelSetup), partitions = 3))
        assertNull(sceneFor(listOf(welcome, modelSetup), partitions = 2))
    }

    @Test
    fun detailWithoutList_returnsNull() {
        assertNull(sceneFor(listOf(welcome, settings), partitions = 3))
    }

    @Test
    fun listAndDetail_delegatesToTheListDetailScene() {
        val scene = assertNotNull(sceneFor(listOf(assistant, settings), partitions = 2))

        assertEquals("main", scene.key)
        assertEquals(listOf(assistant, settings), scene.entries)
    }

    @Test
    fun listDetailAndExtra_delegatesWithAllThree() {
        val scene = assertNotNull(sceneFor(listOf(assistant, settings, modelSetup), partitions = 3))

        assertEquals(listOf(assistant, settings, modelSetup), scene.entries)
    }

    @Test
    fun listBelowANonPaneEntry_isNotInTheTrailingRun() {
        // A pane entry below a non-pane one is not part of the scene the library would build.
        assertNull(sceneFor(listOf(assistant, welcome, lockSetup), partitions = 3))
    }

    @Test
    fun singlePartition_stillYieldsToTheSinglePaneStrategy() {
        assertNull(sceneFor(listOf(assistant, settings), partitions = 1))
    }

    @Test
    fun trailingPanesIncludeList_readsTheRoleMarkers() {
        val include = { metadata: List<Map<String, Any>> -> PesaListDetailSceneStrategy.trailingPanesIncludeList(metadata) }
        assertFalse(include(emptyList()))
        assertFalse(include(listOf(PesaPanes.extra())))
        assertFalse(include(listOf(emptyMap(), PesaPanes.extra())))
        assertFalse(include(listOf(PesaPanes.list {}, emptyMap(), PesaPanes.extra())))
        assertFalse(include(listOf(PesaPanes.overlay())))
        assertTrue(include(listOf(PesaPanes.list {})))
        assertTrue(include(listOf(PesaPanes.list {}, PesaPanes.detail(), PesaPanes.extra())))
        assertTrue(include(listOf(emptyMap(), PesaPanes.list {}, PesaPanes.extra())))
    }

    @Test
    fun withoutAutoFocus_turnsOffOnlyTheFocusMove() {
        val hinge = Rect(400f, 0f, 420f, 800f)
        val directive = PaneScaffoldDirective(
            maxHorizontalPartitions = 2,
            horizontalPartitionSpacerSize = 24.dp,
            maxVerticalPartitions = 1,
            verticalPartitionSpacerSize = 8.dp,
            defaultPanePreferredWidth = 360.dp,
            defaultPanePreferredHeight = 412.dp,
            excludedBounds = listOf(hinge),
            shouldAutoFocusCurrentDestination = true,
        )

        val result = directive.withoutAutoFocus()

        assertFalse(result.shouldAutoFocusCurrentDestination)
        assertEquals(2, result.maxHorizontalPartitions)
        assertEquals(24.dp, result.horizontalPartitionSpacerSize)
        assertEquals(1, result.maxVerticalPartitions)
        assertEquals(8.dp, result.verticalPartitionSpacerSize)
        assertEquals(360.dp, result.defaultPanePreferredWidth)
        assertEquals(412.dp, result.defaultPanePreferredHeight)
        assertEquals(listOf(hinge), result.excludedBounds)
    }

    private fun sceneFor(entries: List<NavEntry<String>>, partitions: Int): Scene<String>? =
        with(PesaListDetailSceneStrategy(listDetail(partitions))) { SceneStrategyScope<String>().calculateScene(entries) }

    private fun listDetail(partitions: Int) = ListDetailSceneStrategy<String>(
        shouldHandleSinglePaneLayout = false,
        backNavigationBehavior = BackNavigationBehavior.PopUntilScaffoldValueChange,
        directive = PaneScaffoldDirective(
            maxHorizontalPartitions = partitions,
            horizontalPartitionSpacerSize = 0.dp,
            maxVerticalPartitions = 1,
            verticalPartitionSpacerSize = 0.dp,
            defaultPanePreferredWidth = 360.dp,
            excludedBounds = emptyList(),
        ),
        adaptStrategies = ListDetailPaneScaffoldDefaults.adaptStrategies(),
        paneExpansionDragHandle = null,
        paneExpansionState = null,
    )
}
