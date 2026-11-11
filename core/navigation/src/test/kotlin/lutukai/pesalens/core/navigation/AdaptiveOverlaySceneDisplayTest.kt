package lutukai.pesalens.core.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.percentOffset
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

private data object Chat : PesaDestination

private data object CloudConsent : PesaDestination

@RunWith(RobolectricTestRunner::class)
class AdaptiveOverlaySceneDisplayTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val backStack = mutableStateListOf<NavKey>(Chat, CloudConsent)
    private var backs = 0

    @Test
    fun sheetMode_showsSheetOverTheEntryBelow_andScrimTapCallsBack() {
        showOverlay(useSheet = true)

        composeRule.onNodeWithText("Chat").assertIsDisplayed()
        composeRule.onNodeWithText("Use cloud AI?").assertIsDisplayed()

        val closeSheet = RuntimeEnvironment.getApplication().getString(androidx.compose.ui.R.string.close_sheet)
        composeRule.onNodeWithContentDescription(closeSheet).performTouchInput { click(percentOffset(0.5f, 0.1f)) }
        composeRule.waitForIdle()

        assertEquals(1, backs)
        assertEquals(listOf<NavKey>(Chat), backStack.toList())
        composeRule.onAllNodesWithText("Use cloud AI?").assertCountEquals(0)
    }

    @Test
    @Config(qualifiers = "w1000dp-h800dp")
    fun dialogMode_capsWidth_andScrimTapCallsBack() {
        showOverlay(useSheet = false)

        composeRule.onNodeWithText("Chat").assertIsDisplayed()
        val bodyWidth = composeRule.onNodeWithTag(OverlayBodyTag).assertIsDisplayed().fetchSemanticsNode().size.width
        val maxPx = with(composeRule.density) { 560.dp.toPx() }
        assertTrue(bodyWidth <= maxPx, "overlay body $bodyWidth px wider than 560dp ($maxPx px)")

        composeRule.onNode(isDialog()).performTouchInput { click(percentOffset(0.02f, 0.02f)) }
        composeRule.waitForIdle()

        assertEquals(1, backs)
        assertEquals(listOf<NavKey>(Chat), backStack.toList())
        composeRule.onAllNodesWithText("Use cloud AI?").assertCountEquals(0)
    }

    private fun showOverlay(useSheet: Boolean) {
        composeRule.setContent {
            PesaLensTheme(darkTheme = false) {
                NavDisplay(
                    backStack = backStack,
                    onBack = {
                        backs++
                        backStack.removeAt(backStack.lastIndex)
                    },
                    sceneStrategies = listOf(AdaptiveOverlaySceneStrategy(useSheet)),
                    entryProvider = entryProvider {
                        entry<Chat> { Text("Chat") }
                        entry<CloudConsent>(metadata = PesaPanes.overlay()) {
                            Text("Use cloud AI?", Modifier.fillMaxWidth().testTag(OverlayBodyTag))
                        }
                    },
                )
            }
        }
    }

    private companion object {
        const val OverlayBodyTag = "overlay-body"
    }
}
