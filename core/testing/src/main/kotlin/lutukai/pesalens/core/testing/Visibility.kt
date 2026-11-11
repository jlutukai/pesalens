package lutukai.pesalens.core.testing

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.text.TextLayoutResult
import kotlin.math.abs

/**
 * Asserts the node is displayed and not clipped: its visible bounds, cut by every clipping ancestor and the window,
 * are its full size. `assertIsDisplayed` alone passes for a node that is only partly visible.
 */
fun SemanticsNodeInteraction.assertFullyDisplayed(): SemanticsNodeInteraction {
    assertIsDisplayed()
    val node = fetchSemanticsNode()
    val visible = node.boundsInWindow
    if (abs(visible.width - node.size.width) > 1f || abs(visible.height - node.size.height) > 1f) {
        throw AssertionError("Node is clipped: visible $visible of size ${node.size} (${node.config})")
    }
    return this
}

/** Asserts the text node's text is laid out in full: no line ellipsized and nothing drawn past its bounds. */
fun SemanticsNodeInteraction.assertTextNotTruncated(): SemanticsNodeInteraction {
    val layouts = mutableListOf<TextLayoutResult>()
    val getLayout = fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action
        ?: throw AssertionError("Node has no text layout")
    getLayout(layouts)
    val layout = layouts.single()
    val ellipsized = (0 until layout.lineCount).any(layout::isLineEllipsized)
    if (ellipsized || layout.hasVisualOverflow) {
        throw AssertionError("Text \"${layout.layoutInput.text}\" is truncated (ellipsized=$ellipsized, overflow=${layout.hasVisualOverflow})")
    }
    return this
}
