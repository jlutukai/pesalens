package lutukai.pesalens.core.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope

/**
 * [ListDetailSceneStrategy], but only for back stacks whose trailing run of pane entries (the entries the
 * list-detail scene would gather) includes a [list][PesaPanes.list] pane. Otherwise it returns null, so the entry
 * falls through to a single, full-window pane.
 *
 * Without this, an extra pane opened with no list beneath it, such as app lock setup during onboarding or model
 * setup from Welcome, became an extra-only scene: a 360dp strip beside an empty detail pane.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class PesaListDetailSceneStrategy<T : Any>(private val listDetail: ListDetailSceneStrategy<T>) : SceneStrategy<T> {

    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        if (!trailingPanesIncludeList(entries.map { it.metadata })) return null
        return with(listDetail) { this@calculateScene.calculateScene(entries) }
    }

    companion object {
        /**
         * Whether the trailing run of entries carrying a [PesaPanes.PaneRole], read from the top of the back stack down
         * to the first entry without one, includes a list pane. [metadata] is in back stack order, bottom first.
         */
        fun trailingPanesIncludeList(metadata: List<Map<String, Any>>): Boolean {
            for (entry in metadata.asReversed()) {
                when (entry[PesaPanes.PaneRoleKey] as? PesaPanes.PaneRole) {
                    PesaPanes.PaneRole.List -> return true
                    PesaPanes.PaneRole.Detail, PesaPanes.PaneRole.Extra -> Unit
                    null -> return false
                }
            }
            return false
        }
    }
}

/**
 * [PesaListDetailSceneStrategy] over a remembered [ListDetailSceneStrategy] with [directive], minus the scaffold's
 * focus move (see [withoutAutoFocus]).
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun <T : Any> rememberPesaListDetailSceneStrategy(directive: PaneScaffoldDirective): PesaListDetailSceneStrategy<T> {
    val listDetail = rememberListDetailSceneStrategy<T>(directive = directive.withoutAutoFocus())
    return remember(listDetail) { PesaListDetailSceneStrategy(listDetail) }
}

/**
 * [this] directive with the scaffold's focus move turned off. Left on, the scaffold focuses the current pane whenever
 * it changes. In touch mode the only focusable control in the chat pane is the composer, so finishing onboarding or
 * closing Settings beside the chat opened the keyboard unasked. Single-pane scenes never move focus, so panes now
 * behave the same at every width. ([PaneScaffoldDirective.copy] cannot change this flag.)
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun PaneScaffoldDirective.withoutAutoFocus(): PaneScaffoldDirective = PaneScaffoldDirective(
    maxHorizontalPartitions = maxHorizontalPartitions,
    horizontalPartitionSpacerSize = horizontalPartitionSpacerSize,
    maxVerticalPartitions = maxVerticalPartitions,
    verticalPartitionSpacerSize = verticalPartitionSpacerSize,
    defaultPanePreferredWidth = defaultPanePreferredWidth,
    defaultPanePreferredHeight = defaultPanePreferredHeight,
    excludedBounds = excludedBounds,
    shouldAutoFocusCurrentDestination = false,
)
