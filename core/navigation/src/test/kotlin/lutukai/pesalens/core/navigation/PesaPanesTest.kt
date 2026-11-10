package lutukai.pesalens.core.navigation

import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldDefaults
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.SceneStrategyScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PesaPanesTest {

    @Test
    fun listDetailAndExtra_shareOneListDetailScene() {
        val list = NavEntry("list", metadata = PesaPanes.list(detailPlaceholder = {})) { }
        val detail = NavEntry("detail", metadata = PesaPanes.detail()) { }
        val extra = NavEntry("extra", metadata = PesaPanes.extra()) { }

        val scene = with(threePaneStrategy()) {
            SceneStrategyScope<String>().calculateScene(listOf(list, detail, extra))
        }

        assertNotNull(scene)
        assertEquals("main", scene.key)
        assertEquals(listOf(list, detail, extra), scene.entries)
    }

    @Test
    fun overlay_carriesTheOverlayKey() {
        assertEquals("pesa.overlay", PesaPanes.OverlayKey)
        assertTrue(PesaPanes.OverlayKey in PesaPanes.overlay())
    }

    private fun threePaneStrategy() = ListDetailSceneStrategy<String>(
        shouldHandleSinglePaneLayout = false,
        backNavigationBehavior = BackNavigationBehavior.PopUntilScaffoldValueChange,
        directive = PaneScaffoldDirective(
            maxHorizontalPartitions = 3,
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
