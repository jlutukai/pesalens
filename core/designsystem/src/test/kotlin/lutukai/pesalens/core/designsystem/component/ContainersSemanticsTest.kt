package lutukai.pesalens.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import lutukai.pesalens.core.designsystem.icon.PesaIcons
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ContainersSemanticsTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun topBar_backButton_hasDescriptionAnd48dp() {
        var backs = 0
        composeRule.setContent {
            PesaLensTheme(darkTheme = false) {
                PesaTopBar("Food", subtitle = "November 2026", onBack = { backs++ }, backContentDescription = "Back to conversation")
            }
        }
        val back = composeRule.onNodeWithContentDescription("Back to conversation")
        back.assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
        val bounds = back.fetchSemanticsNode().touchBoundsInRoot
        val minPx = with(composeRule.density) { 48.dp.toPx() }
        assertTrue(bounds.width >= minPx - 0.5f && bounds.height >= minPx - 0.5f, "back touch bounds $bounds < $minPx")
        back.performClick()
        assertEquals(1, backs)
    }

    @Test
    fun expandable_togglesContent() {
        composeRule.setContent {
            PesaLensTheme(darkTheme = false) {
                PesaExpandable("Privacy and data", PesaIcons.ShieldCheck) { Text("Stays on this phone") }
            }
        }
        val title = composeRule.onNodeWithText("Privacy and data")
        title.assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
        title.assert(SemanticsMatcher.keyIsDefined(SemanticsActions.Expand))
        title.assert(SemanticsMatcher.keyNotDefined(SemanticsActions.Collapse))
        composeRule.onAllNodesWithText("Stays on this phone").assertCountEquals(0)

        title.performClick()

        composeRule.onNodeWithText("Stays on this phone").assertIsDisplayed()
        title.assert(SemanticsMatcher.keyIsDefined(SemanticsActions.Collapse))
        title.assert(SemanticsMatcher.keyNotDefined(SemanticsActions.Expand))

        title.performClick()
        composeRule.onAllNodesWithText("Stays on this phone").assertCountEquals(0)
    }

    @Test
    fun downloadProgress_exposesRangeInfo() {
        composeRule.setContent {
            PesaLensTheme(darkTheme = false) { PesaDownloadProgress(42, "Model download") }
        }
        composeRule.onNodeWithContentDescription("Model download").assert(
            SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo, ProgressBarRangeInfo(0.42f, 0f..1f)),
        )
    }

    @Test
    fun callout_liveRegion_isPolite() {
        composeRule.setContent {
            PesaLensTheme(darkTheme = false) {
                Column {
                    PesaCallout("Live note", PesaIcons.Info, CalloutTone.Info, liveRegion = true)
                    PesaCallout("Static note", PesaIcons.Info, CalloutTone.Plain)
                }
            }
        }
        composeRule.onNodeWithText("Live note", useUnmergedTree = false).assert(
            SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite),
        )
        composeRule.onNodeWithText("Static note").assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.LiveRegion))
    }

    @Test
    fun topBar_backWithoutDescription_throws() {
        assertFailsWith<IllegalArgumentException> {
            composeRule.setContent { PesaLensTheme(darkTheme = false) { PesaTopBar("Food", onBack = {}) } }
            composeRule.waitForIdle()
        }
    }

    private fun setDialog(onDismiss: () -> Unit) = composeRule.setContent {
        PesaLensTheme(darkTheme = false) {
            PesaAlertDialog(
                title = "Reset sample data?",
                onDismiss = onDismiss,
                confirm = { PesaButton("Confirm", {}) },
                dismiss = { PesaButton("Cancel", {}) },
                icon = PesaIcons.ArrowCounterClockwise,
                subtitle = "Body copy",
            )
        }
    }

    @Test
    fun alertDialog_titleAndBody_areSeparateReadableNodes_noEmptyClickAction() {
        setDialog {}
        composeRule.onNodeWithText("Reset sample data?").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
        composeRule.onNodeWithText("Body copy").assertIsDisplayed()
        composeRule.onAllNodes(hasClickAction()).assertCountEquals(2)
    }

    @Test
    fun alertDialog_tapOnScrim_dismisses_tapInside_doesNot() {
        var dismissed = 0
        setDialog { dismissed++ }
        composeRule.onNodeWithText("Body copy").performClick()
        assertEquals(0, dismissed)
        composeRule.onNode(isDialog()).performTouchInput { click(Offset(4f, 4f)) }
        composeRule.waitForIdle()
        assertTrue(dismissed >= 1, "scrim tap should dismiss")
    }
}
