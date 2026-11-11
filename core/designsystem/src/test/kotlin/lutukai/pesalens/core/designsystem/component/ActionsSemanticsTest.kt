package lutukai.pesalens.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.collections.immutable.persistentListOf
import lutukai.pesalens.core.designsystem.icon.PesaIcons
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class ActionsSemanticsTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val radio = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton)

    private fun modeOptions(onDeviceEnabled: Boolean) = persistentListOf(
        SegmentOption("cloud", "Cloud AI", PesaIcons.Cloud),
        SegmentOption("device", "On-device AI", PesaIcons.Cpu, enabled = onDeviceEnabled),
    )

    @Test
    fun segmentedControl_exposesRadioSemantics() {
        composeRule.setContent {
            PesaLensTheme(darkTheme = false) {
                var selected by remember { mutableStateOf("cloud") }
                PesaSegmentedControl(modeOptions(true), selected, { selected = it }, "Assistant mode")
            }
        }
        composeRule.onNodeWithText("Cloud AI").assert(radio).assertIsSelected()
        composeRule.onNodeWithText("On-device AI").assert(radio).assertIsNotSelected()

        composeRule.onNodeWithText("On-device AI").performClick()

        composeRule.onNodeWithText("Cloud AI").assertIsNotSelected()
        composeRule.onNodeWithText("On-device AI").assertIsSelected()
    }

    @Test
    fun segmentedControl_disabledOption_notClickable() {
        val picked = mutableListOf<String>()
        composeRule.setContent {
            PesaLensTheme(darkTheme = false) {
                PesaSegmentedControl(modeOptions(false), "cloud", { picked += it }, "Assistant mode")
            }
        }
        composeRule.onNodeWithText("On-device AI").assertIsNotEnabled().performClick()
        composeRule.onNodeWithText("Cloud AI").assertIsEnabled()
        assertEquals(emptyList(), picked)
    }

    @Test
    fun amountField_filtersNonDigits() {
        val emitted = mutableListOf<String>()
        composeRule.setContent {
            PesaLensTheme(darkTheme = false) {
                var value by remember { mutableStateOf("") }
                PesaAmountField(
                    value = value,
                    onValueChange = { value = it; emitted += it },
                    placeholder = "e.g. 30,000",
                    contentDescription = "Amount",
                    prefix = null,
                )
            }
        }
        composeRule.onNodeWithContentDescription("Amount").performTextInput("3a0,0b00")
        assertEquals("30000", emitted.last())
        composeRule.onNodeWithContentDescription("Amount").assertTextEquals("30,000")
    }

    @Test
    fun pinPad_tapsDigitsAndDelete() {
        val digits = mutableListOf<Char>()
        var deletes = 0
        composeRule.setContent {
            PesaLensTheme(darkTheme = false) {
                PesaPinPad({ digits += it }, { deletes++ }, "Delete digit")
            }
        }
        composeRule.onNodeWithText("7").performClick()
        composeRule.onNodeWithText("0").performClick()
        composeRule.onNodeWithContentDescription("Delete digit").performClick()
        assertEquals(listOf('7', '0'), digits)
        assertEquals(1, deletes)
    }

    private fun SemanticsNodeInteraction.assertTouchAtLeast48(width: Boolean = true) {
        val b = fetchSemanticsNode().touchBoundsInRoot
        val minPx = with(composeRule.density) { 48.dp.toPx() }
        assertTrue(b.height >= minPx - 0.5f, "touch height ${b.height} < $minPx")
        if (width) assertTrue(b.width >= minPx - 0.5f, "touch width ${b.width} < $minPx")
    }

    @Test
    @Config(qualifiers = "w360dp-h1000dp-xhdpi")
    fun allActions_meetMinTouchTarget() {
        composeRule.setContent {
            PesaLensTheme(darkTheme = false) {
                Column {
                    PesaButton("Save", {})
                    PesaSuggestionChip("Chip", {})
                    PesaSegmentedControl(modeOptions(true), "cloud", {}, "Assistant mode")
                    PesaStepperButton(PesaIcons.Plus, "Increase", {})
                    PesaStepperButton(PesaIcons.Plus, "Increase dense", {}, width = 44.dp)
                    PesaComposer("", {}, {}, sendEnabled = true, placeholder = "Ask", sendContentDescription = "Send")
                    PesaPinPad({}, {}, "Delete digit")
                }
            }
        }
        composeRule.onNodeWithText("Save").assertTouchAtLeast48(width = false)
        composeRule.onNodeWithText("Chip").assertTouchAtLeast48(width = false)
        composeRule.onNodeWithText("Cloud AI").assertTouchAtLeast48(width = false)
        composeRule.onNodeWithText("On-device AI").assertTouchAtLeast48(width = false)
        composeRule.onNodeWithContentDescription("Increase").assertTouchAtLeast48()
        composeRule.onNodeWithContentDescription("Increase dense").assertTouchAtLeast48()
        composeRule.onNodeWithContentDescription("Send").assertTouchAtLeast48()
        composeRule.onNodeWithText("5").assertTouchAtLeast48()
        composeRule.onNodeWithContentDescription("Delete digit").assertTouchAtLeast48()
    }

    @Test
    fun thousandsGrouping_groupsAndMapsCaret() {
        val result = ThousandsGrouping.filter(androidx.compose.ui.text.AnnotatedString("1234567"))
        assertEquals("1,234,567", result.text.text)
        assertEquals(9, result.offsetMapping.originalToTransformed(7))
        assertEquals(7, result.offsetMapping.transformedToOriginal(9))
        assertEquals(3, result.offsetMapping.transformedToOriginal(4))
        assertEquals("", ThousandsGrouping.filter(androidx.compose.ui.text.AnnotatedString("")).text.text)
    }
}
