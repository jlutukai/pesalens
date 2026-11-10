package lutukai.pesalens.core.navigation

import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable

/**
 * `NavEntry` metadata that places an entry in PesaLens' scenes. List, detail and extra panes all join the one
 * list-detail scene keyed `"main"`; [overlay] entries are shown by [AdaptiveOverlaySceneStrategy].
 */
object PesaPanes {
    /** Metadata key that marks an entry for [AdaptiveOverlaySceneStrategy]. */
    const val OverlayKey: String = "pesa.overlay"

    private const val MainSceneKey = "main"

    /** List pane. [detailPlaceholder] fills the detail pane while no detail entry is on the back stack. */
    fun list(detailPlaceholder: @Composable () -> Unit): Map<String, Any> =
        ListDetailSceneStrategy.listPane(sceneKey = MainSceneKey, detailPlaceholder = { detailPlaceholder() })

    fun detail(): Map<String, Any> = ListDetailSceneStrategy.detailPane(sceneKey = MainSceneKey)

    fun extra(): Map<String, Any> = ListDetailSceneStrategy.extraPane(sceneKey = MainSceneKey)

    /** Bottom sheet on compact widths, dialog otherwise. The scene supplies the chrome; the entry is the body. */
    fun overlay(): Map<String, Any> = mapOf(OverlayKey to true)
}
