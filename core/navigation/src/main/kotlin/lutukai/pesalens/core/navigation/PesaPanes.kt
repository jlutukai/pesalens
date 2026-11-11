package lutukai.pesalens.core.navigation

import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable

/**
 * `NavEntry` metadata that places an entry in PesaLens' scenes. List, detail and extra panes all join the one
 * list-detail scene keyed `"main"`, shown by [PesaListDetailSceneStrategy]; [overlay] entries are shown by
 * [AdaptiveOverlaySceneStrategy].
 *
 * Each pane's metadata carries its [PaneRole] under [PaneRoleKey] beside the library's own, so
 * [PesaListDetailSceneStrategy] can tell whether a list pane is among the entries the scene would show.
 */
object PesaPanes {
    /** Metadata key that marks an entry for [AdaptiveOverlaySceneStrategy]. */
    const val OverlayKey: String = "pesa.overlay"

    /** Metadata key holding the entry's [PaneRole]. */
    const val PaneRoleKey: String = "pesa.paneRole"

    enum class PaneRole { List, Detail, Extra }

    private const val MainSceneKey = "main"

    /** List pane. [detailPlaceholder] fills the detail pane while no detail entry is on the back stack. */
    fun list(detailPlaceholder: @Composable () -> Unit): Map<String, Any> =
        ListDetailSceneStrategy.listPane(sceneKey = MainSceneKey, detailPlaceholder = { detailPlaceholder() }) +
            (PaneRoleKey to PaneRole.List)

    fun detail(): Map<String, Any> = ListDetailSceneStrategy.detailPane(sceneKey = MainSceneKey) + (PaneRoleKey to PaneRole.Detail)

    /** Extra pane. It sits beside a list (and detail) pane; without a list pane below it, it shows as a single pane. */
    fun extra(): Map<String, Any> = ListDetailSceneStrategy.extraPane(sceneKey = MainSceneKey) + (PaneRoleKey to PaneRole.Extra)

    /** Bottom sheet on compact widths, dialog otherwise. The scene supplies the chrome; the entry is the body. */
    fun overlay(): Map<String, Any> = mapOf(OverlayKey to true)
}
