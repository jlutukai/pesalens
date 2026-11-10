package lutukai.pesalens.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.designsystem.theme.PesaSpacing

enum class CalloutTone {
    /** tintSubtle background, border. */
    Info,

    /** tintSubtle background, accent border. */
    Accent,

    /** warn background, border and text. */
    Warn,

    /** surfaceAlt background, no border. */
    Plain,
}

/** Inline notice. With [liveRegion] the text is announced politely when it changes. */
@Composable
fun PesaCallout(
    text: String,
    icon: ImageVector,
    tone: CalloutTone,
    modifier: Modifier = Modifier,
    title: String? = null,
    footnote: String? = null,
    liveRegion: Boolean = false,
) {
    val colors = PesaLensTheme.colors
    val type = PesaLensTheme.type
    val fill = when (tone) {
        CalloutTone.Info, CalloutTone.Accent -> colors.tintSubtle
        CalloutTone.Warn -> colors.warnBackground
        CalloutTone.Plain -> colors.surfaceAlt
    }
    val edge = when (tone) {
        CalloutTone.Info -> colors.border
        CalloutTone.Accent -> colors.accent
        CalloutTone.Warn -> colors.warnBorder
        CalloutTone.Plain -> null
    }
    val ink = if (tone == CalloutTone.Warn) colors.warnText else colors.text
    val iconInk = when (tone) {
        CalloutTone.Warn -> colors.warnText
        CalloutTone.Info, CalloutTone.Accent -> colors.accentText
        CalloutTone.Plain -> colors.textMuted
    }
    Surface(
        modifier = modifier.fillMaxWidth().semantics(mergeDescendants = true) { if (liveRegion) this.liveRegion = LiveRegionMode.Polite },
        shape = PesaLensTheme.shapes.tile,
        color = fill,
        contentColor = ink,
        border = edge?.let { BorderStroke(1.dp, it) },
    ) {
        Row(Modifier.padding(PesaSpacing.md), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
            Icon(icon, contentDescription = null, tint = iconInk, modifier = Modifier.size(20.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(PesaSpacing.xs)) {
                if (title != null) Text(title, style = type.bodyStrong, color = ink)
                Text(text, style = type.body, color = ink)
                if (footnote != null) Text(footnote, style = type.meta, color = colors.textMuted)
            }
        }
    }
}
