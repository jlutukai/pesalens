package lutukai.pesalens.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import lutukai.pesalens.core.designsystem.icon.PesaIcons
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h1400dp-xhdpi")
class ActionsScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun actions_light() = capture(darkTheme = false, path = "actions_light.png")

    @Test
    fun actions_dark() = capture(darkTheme = true, path = "actions_dark.png")

    private fun capture(darkTheme: Boolean, path: String) {
        composeRule.setContent { PesaLensTheme(darkTheme) { ActionsSheet() } }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/$path")
    }
}

@Composable
private fun ActionsSheet() {
    val colors = PesaLensTheme.colors
    Column(
        Modifier.background(colors.background).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        PesaButtonStyle.entries.forEach { style ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                PesaButton(style.name, {}, style = style)
                PesaButton("Disabled", {}, style = style, enabled = false)
                PesaButton("Icon", {}, style = style, leadingIcon = PesaIcons.Plus)
            }
        }
        PesaButton("Large button", {}, Modifier.fillMaxWidth(), style = PesaButtonStyle.Tonal, large = true)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PesaSuggestionChip("Can I afford a laptop?", {})
            PesaSuggestionChip("Use sample values", {})
        }
        PesaSegmentedControl(
            options = persistentListOf(
                SegmentOption("cloud", "Cloud AI", PesaIcons.Cloud),
                SegmentOption("device", "On-device AI", PesaIcons.Cpu, enabled = false),
            ),
            selected = "cloud",
            onSelect = {},
            contentDescription = "Assistant mode",
            modifier = Modifier.fillMaxWidth(),
        )
        PesaSegmentedControl(
            options = persistentListOf(
                SegmentOption("light", "Light", PesaIcons.Sun),
                SegmentOption("dark", "Dark", PesaIcons.Moon),
            ),
            selected = "dark",
            onSelect = {},
            contentDescription = "Theme",
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            PesaStepperButton(PesaIcons.Minus, "Decrease", {})
            PesaStepperButton(PesaIcons.Plus, "Increase", {})
            PesaStepperButton(PesaIcons.Minus, "Decrease", {}, enabled = false)
            PesaStepperButton(PesaIcons.Plus, "Increase dense", {}, width = 44.dp)
        }
        PesaAmountField("", {}, placeholder = "e.g. 30,000")
        PesaAmountField("30000", {}, placeholder = "e.g. 30,000", isError = true, textAlign = TextAlign.End)
        PesaAmountField("1250000", {}, placeholder = "e.g. 30,000")
        PesaComposer("", {}, {}, sendEnabled = false, placeholder = "Ask about your money", sendContentDescription = "Send")
        PesaComposer("Can I afford a laptop?", {}, {}, sendEnabled = true, placeholder = "Ask about your money", sendContentDescription = "Send")
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Create a 4-digit PIN", style = PesaLensTheme.type.titleLarge, color = colors.text)
            PesaPinDots(filled = 2, isError = false)
            PesaPinDots(filled = 4, isError = true)
            PesaPinPad({}, {}, "Delete digit")
        }
    }
}
