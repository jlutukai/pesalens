package lutukai.pesalens.core.designsystem.component

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import lutukai.pesalens.core.designsystem.icon.PesaIcons
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val Bar = "bar"
private const val Title = "Food and groceries"
private const val Subtitle = "November 2026 · 1–9 Nov"

/** The top bar keeps its 64dp at normal font sizes and grows at 200% instead of cutting its title and subtitle off. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w412dp-h892dp-420dpi")
class TopBarFontScaleTest {
    @get:Rule
    val composeRule = createComposeRule()

    private fun show(fontScale: Float) = composeRule.setContent {
        val density = LocalDensity.current
        CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
            PesaLensTheme(darkTheme = false) {
                PesaTopBar(
                    Title,
                    modifier = Modifier.testTag(Bar),
                    subtitle = Subtitle,
                    onBack = {},
                    backContentDescription = "Back to conversation",
                    titleStyle = PesaLensTheme.type.titleSmall,
                ) { PesaPill("Sample", PillTone.Dashed, icon = PesaIcons.Database, height = 24.dp) }
            }
        }
    }

    @Test
    fun normalFontScale_isExactly64dp() {
        show(fontScale = 1f)
        composeRule.onNodeWithTag(Bar).assertHeightIsEqualTo(64.dp)
    }

    @Test
    fun fontScale2_showsTitleAndSubtitleInFull() {
        show(fontScale = 2f)
        composeRule.onNodeWithText(Title).assertNotCut()
        composeRule.onNodeWithText(Subtitle).assertNotCut()
        assertTrue(composeRule.onNodeWithTag(Bar).getUnclippedBoundsInRoot().height > 64.dp)
    }

    /** Displayed, not clipped by the bar, and laid out without an ellipsis or overflow. */
    private fun SemanticsNodeInteraction.assertNotCut() {
        assertIsDisplayed()
        val node = fetchSemanticsNode()
        val visible = node.boundsInWindow
        assertTrue(abs(visible.height - node.size.height) <= 1f && abs(visible.width - node.size.width) <= 1f, "clipped: $visible vs ${node.size}")
        val layouts = mutableListOf<TextLayoutResult>()
        node.config[SemanticsActions.GetTextLayoutResult].action?.invoke(layouts)
        val layout = layouts.single()
        assertFalse(layout.hasVisualOverflow, "overflow")
        assertFalse((0 until layout.lineCount).any(layout::isLineEllipsized), "ellipsized")
    }
}
