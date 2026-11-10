package lutukai.pesalens.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.unit.dp
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme

/** 10dp ratio bar. [fraction] is clamped to 0..1; pass a warn [color] for overflow. Decorative (hidden from accessibility). */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PesaRatioBar(fraction: Float, color: Color = PesaLensTheme.colors.accent, modifier: Modifier = Modifier) {
    LinearProgressIndicator(
        progress = { fraction.coerceIn(0f, 1f) },
        modifier = modifier.fillMaxWidth().height(10.dp).clearAndSetSemantics {},
        color = color,
        trackColor = PesaLensTheme.colors.surfaceAlt,
    )
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
        trackColor = PesaLensTheme.colors.surfaceAlt,
    )
}
