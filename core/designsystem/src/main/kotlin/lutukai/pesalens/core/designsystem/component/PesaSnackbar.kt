package lutukai.pesalens.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.designsystem.theme.PesaSpacing

/** Snackbar host with the inverse look: text background, background foreground, 12dp radius, 16dp from the sides. */
@Composable
fun PesaSnackbarHost(state: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(state, modifier.padding(horizontal = PesaSpacing.lg)) { data ->
        PesaSnackbarContent(data.visuals.message)
    }
}

@Composable
internal fun PesaSnackbarContent(message: String, modifier: Modifier = Modifier) {
    val colors = PesaLensTheme.colors
    Surface(
        modifier = modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
        shape = PesaLensTheme.shapes.button,
        color = colors.text,
        contentColor = colors.background,
        shadowElevation = 8.dp,
    ) {
        Row(
            Modifier.padding(horizontal = PesaSpacing.lg, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PesaSpacing.md),
        ) {
            Text(message, style = PesaLensTheme.type.bodySmall.copy(lineHeight = 19.6.sp), color = colors.background)
        }
    }
}
