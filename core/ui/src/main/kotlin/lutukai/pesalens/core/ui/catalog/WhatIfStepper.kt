package lutukai.pesalens.core.ui.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import lutukai.pesalens.core.designsystem.component.PesaStepperButton
import lutukai.pesalens.core.designsystem.icon.PesaIcons
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme

/**
 * "label  [-]  value  [+]". The component only reports taps; the caller applies the step. With [announce] the value is a
 * polite live region, so a screen reader speaks the new figure after a step.
 */
@Composable
fun WhatIfStepper(
    label: String,
    value: String,
    onDown: () -> Unit,
    onUp: () -> Unit,
    downEnabled: Boolean,
    downDescription: String,
    upDescription: String,
    announce: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = PesaLensTheme.colors
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = PesaLensTheme.type.bodySmall, color = colors.text, modifier = Modifier.weight(1f))
        PesaStepperButton(PesaIcons.Minus, downDescription, onDown, enabled = downEnabled, containerColor = colors.surface)
        Text(
            value,
            style = PesaLensTheme.type.bodyStrong.copy(fontFeatureSettings = "tnum"),
            color = colors.text,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(min = 92.dp)
                .then(if (announce) Modifier.semantics { liveRegion = LiveRegionMode.Polite } else Modifier),
        )
        PesaStepperButton(PesaIcons.Plus, upDescription, onUp, containerColor = colors.surface)
    }
}
