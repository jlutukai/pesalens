package lutukai.pesalens.core.ui.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.border
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import lutukai.pesalens.core.designsystem.component.PesaSuggestionChip
import lutukai.pesalens.core.designsystem.icon.PesaIcons
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.designsystem.theme.PesaSpacing
import lutukai.pesalens.core.model.AssistantMode
import lutukai.pesalens.core.ui.R

/** Caps the width at [fraction] of what the parent offers, while still wrapping the content. */
private fun Modifier.maxWidthFraction(fraction: Float): Modifier = layout { measurable, constraints ->
    val cap = if (constraints.hasBoundedWidth) (constraints.maxWidth * fraction).toInt() else Constraints.Infinity
    val placeable = measurable.measure(constraints.copy(maxWidth = minOf(constraints.maxWidth, cap), minWidth = 0))
    layout(placeable.width, placeable.height) { placeable.place(0, 0) }
}

/** The user's message: tint fill, aligned end, at most 80% of the width. */
@Composable
fun UserBubble(text: String) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
        Text(
            text,
            style = PesaLensTheme.type.body,
            color = PesaLensTheme.colors.text,
            modifier = Modifier
                .maxWidthFraction(0.8f)
                .background(PesaLensTheme.colors.tint, PesaLensTheme.shapes.bubbleUser)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        )
    }
}

/** The assistant's message: surface fill and border, aligned start, at most 88% of the width. */
@Composable
fun AssistantBubble(text: String) {
    val colors = PesaLensTheme.colors
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
        Text(
            text,
            style = PesaLensTheme.type.body,
            color = colors.text,
            modifier = Modifier
                .maxWidthFraction(0.88f)
                .background(colors.surface, PesaLensTheme.shapes.bubbleBot)
                .border(BorderStroke(1.dp, colors.border), PesaLensTheme.shapes.bubbleBot)
                .padding(horizontal = 14.dp, vertical = 11.dp),
        )
    }
}

/** Loading bubble: Expressive loading indicator plus [label], announced politely as a status. */
@Composable
fun ThinkingBubble(label: String) {
    val colors = PesaLensTheme.colors
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
        Row(
            Modifier
                .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite }
                .background(colors.surface, PesaLensTheme.shapes.bubbleBot)
                .border(BorderStroke(1.dp, colors.border), PesaLensTheme.shapes.bubbleBot)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            LoadingIndicator(Modifier.size(24.dp), color = colors.accent)
            Text(label, style = PesaLensTheme.type.bodySmall, color = colors.textMuted)
        }
    }
}

/** Caption under assistant replies: which AI interpreted the question. */
@Composable
fun MessageCaption(mode: AssistantMode) {
    val colors = PesaLensTheme.colors
    val (icon, text) = when (mode) {
        AssistantMode.Cloud -> PesaIcons.Cloud to stringResource(R.string.core_ui_caption_cloud)
        AssistantMode.OnDevice -> PesaIcons.CpuFill to stringResource(R.string.core_ui_caption_on_device)
    }
    Row(Modifier.padding(start = PesaSpacing.xs), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Icon(icon, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(12.dp))
        Text(text, style = PesaLensTheme.type.caption, color = colors.textMuted)
    }
}

/** Follow-up suggestion chips; [onClick] receives the chip's index. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SuggestionRow(labels: ImmutableList<String>, onClick: (index: Int) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(PesaSpacing.sm), verticalArrangement = Arrangement.spacedBy(PesaSpacing.sm)) {
        labels.forEachIndexed { index, label -> PesaSuggestionChip(label, onClick = { onClick(index) }) }
    }
}
