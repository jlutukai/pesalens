package lutukai.pesalens.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test
import lutukai.pesalens.core.designsystem.pattern.ChartPattern
import lutukai.pesalens.core.designsystem.pattern.patternFill
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h1100dp-xhdpi")
class ThemeScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun tokens_light() = capture(darkTheme = false, path = "tokens_light.png")

    @Test
    fun tokens_dark() = capture(darkTheme = true, path = "tokens_dark.png")

    private fun capture(darkTheme: Boolean, path: String) {
        composeRule.setContent { PesaLensTheme(darkTheme) { SwatchSheet() } }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/$path")
    }
}

@Composable
private fun SwatchSheet() {
    val colors = PesaLensTheme.colors
    val type = PesaLensTheme.type
    Column(
        Modifier
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        listOf(
            "background" to colors.background, "surface" to colors.surface, "surfaceAlt" to colors.surfaceAlt,
            "text" to colors.text, "textMuted" to colors.textMuted, "accent" to colors.accent,
            "accentText" to colors.accentText, "tint" to colors.tint, "tintSubtle" to colors.tintSubtle,
            "border" to colors.border, "warnText" to colors.warnText, "warnBackground" to colors.warnBackground,
            "warnBorder" to colors.warnBorder, "patternInk" to colors.patternInk, "scrim" to colors.scrim,
        ).forEach { (name, color) -> ColorRow(name, color) }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            colors.chart.forEach { Box(Modifier.size(24.dp).background(it)) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChartPattern.entries.forEachIndexed { i, pattern ->
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .patternFill(pattern, colors.chart[i], colors.patternInk),
                )
            }
        }
        listOf(
            "figure" to type.figure, "display" to type.display, "headline" to type.headline,
            "titleLarge" to type.titleLarge, "titleMedium" to type.titleMedium, "title" to type.title,
            "titleSmall" to type.titleSmall, "subtitle" to type.subtitle, "body" to type.body,
            "bodyStrong" to type.bodyStrong, "label" to type.label, "bodySmall" to type.bodySmall,
            "meta" to type.meta, "kicker" to type.kicker, "caption" to type.caption,
        ).forEach { (name, style) ->
            Text("KSh 65,000  $name", style = style, color = colors.text)
        }
    }
}

@Composable
private fun ColorRow(name: String, color: Color) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(24.dp).background(color))
        Text(name, style = PesaLensTheme.type.meta, color = PesaLensTheme.colors.text)
    }
}
