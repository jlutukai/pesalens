package lutukai.pesalens.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test
import lutukai.pesalens.core.designsystem.icon.PesaIcons
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h2300dp-xhdpi")
class ContainersScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun containers_light() = capture(darkTheme = false, path = "containers_light.png")

    @Test
    fun containers_dark() = capture(darkTheme = true, path = "containers_dark.png")

    private fun capture(darkTheme: Boolean, path: String) {
        composeRule.setContent { PesaLensTheme(darkTheme) { ContainersSheet() } }
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/$path")
    }
}

@Composable
private fun ContainersSheet() {
    val colors = PesaLensTheme.colors
    Column(Modifier.background(colors.background)) {
        PesaTopBar("Food and groceries", subtitle = "November 2026 · 1–9 Nov", onBack = {}, backContentDescription = "Back") {
            PesaPill("Sample", PillTone.Dashed, PesaIcons.Database, height = 24.dp)
        }
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                PesaPill("Cloud AI · online", PillTone.Neutral, PesaIcons.Cloud)
                PesaPill("On-device AI · offline", PillTone.Accent, PesaIcons.CpuFill)
            }
            PesaCard {
                PesaKicker("Purchase estimate · Laptop")
                PesaValueRow("Purchase price", "KSh 90,000")
                PesaValueRow("Total budgeted", "KSh 75,000", strong = true)
                PesaRatioBar(0.33f)
                PesaRatioBar(1.1f, color = colors.warnText)
            }
            PesaCard(border = colors.accent) {
                PesaNavRow("App lock", PesaIcons.LockKey, {}, subtitle = "Off")
                PesaNavRow("Clear conversation", PesaIcons.Broom, {}, showChevron = false, topBorder = true)
            }
            PesaCardRow("Fingerprint or face unlock", "Uses the biometrics already on this phone", PesaIcons.Fingerprint, {})
            Column {
                PesaDownloadProgress(42, "Model download")
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Downloading", style = PesaLensTheme.type.bodySmall, color = colors.text)
                    Text("42%", style = PesaLensTheme.type.bodySmall, color = colors.text)
                }
            }
            PesaCallout("Spending is up from last month.", PesaIcons.Info, CalloutTone.Info)
            PesaCallout("The amount set aside covers this purchase.", PesaIcons.CheckCircle, CalloutTone.Accent)
            PesaCallout("There is no estimated completion date at this savings rate.", PesaIcons.PauseCircle, CalloutTone.Warn, liveRegion = true)
            PesaCallout("Figures are estimates.", PesaIcons.Lightbulb, CalloutTone.Plain, title = "Good to know", footnote = "Not financial advice.")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PesaIconTile(PesaIcons.Cloud, size = 40.dp, iconSize = 19.dp)
                PesaIconTile(PesaIcons.Warning, size = 52.dp, iconSize = 26.dp, tone = TileTone.Warn, shape = PesaLensTheme.shapes.tileLarge)
            }
            PesaExpandable("Privacy and data", PesaIcons.ShieldCheck) {
                Text(
                    "Sample transactions and saved budgets stay in app storage on this phone.",
                    style = PesaLensTheme.type.meta,
                    color = colors.textMuted,
                    modifier = Modifier.padding(start = 38.dp, bottom = 14.dp),
                )
            }
            Box(Modifier.fillMaxWidth().height(300.dp)) {
                PesaAlertDialogContent(
                    title = "Reset sample data?",
                    onDismiss = {},
                    confirm = { PesaButton("Delete and reset", {}, style = PesaButtonStyle.Warn) },
                    dismiss = { PesaButton("Cancel", {}, style = PesaButtonStyle.Text) },
                    icon = PesaIcons.ArrowCounterClockwise,
                    iconTint = colors.warnText,
                    subtitle = "This restores the original sample transactions. Saved budgets and any sample-data changes will be permanently deleted, and the conversation will be cleared.",
                    content = {},
                )
            }
            PesaSnackbarContent("Conversation cleared.")
        }
    }
}
