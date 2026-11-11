package lutukai.pesalens.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.unit.dp
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme

/**
 * 10dp ratio bar: a surfaceAlt track with a 1dp border, the fill inside it. [fraction] is clamped to 0..1; pass a warn
 * [color] for overflow. Decorative (hidden from accessibility).
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PesaRatioBar(fraction: Float, color: Color = PesaLensTheme.colors.accent, modifier: Modifier = Modifier) {
    val colors = PesaLensTheme.colors
    val shape = PesaLensTheme.shapes.track
    Box(
        modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(shape)
            .background(colors.surfaceAlt)
            .border(1.dp, colors.border, shape)
            .padding(1.dp)
            .clearAndSetSemantics {},
    ) {
        LinearProgressIndicator(
            progress = { fraction.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxSize(),
            color = color,
            trackColor = colors.surfaceAlt,
        )
    }
}

/** Wavy determinate download progress with range-info semantics (0..1). */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PesaDownloadProgress(percent: Int, contentDescription: String) {
    val fraction = percent.coerceIn(0, 100) / 100f
    LinearWavyProgressIndicator(
        progress = { fraction },
        modifier = Modifier.fillMaxWidth().clearAndSetSemantics {
            this.contentDescription = contentDescription
            progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f)
        },
        color = PesaLensTheme.colors.accent,
        // The border colour keeps the remaining track visible; surfaceAlt equals the light background.
        trackColor = PesaLensTheme.colors.border,
    )
}
