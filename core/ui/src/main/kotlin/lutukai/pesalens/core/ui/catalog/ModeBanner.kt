package lutukai.pesalens.core.ui.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import lutukai.pesalens.core.designsystem.component.PesaPill
import lutukai.pesalens.core.designsystem.component.PillTone
import lutukai.pesalens.core.designsystem.icon.PesaIcons
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.model.AssistantMode
import lutukai.pesalens.core.ui.R

/** Header pill naming the active mode: neutral cloud, accent on-device. */
@Composable
fun ModePill(mode: AssistantMode) {
    when (mode) {
        AssistantMode.Cloud -> PesaPill(stringResource(R.string.core_ui_mode_cloud_online), PillTone.Neutral, PesaIcons.Cloud)
        AssistantMode.OnDevice -> PesaPill(stringResource(R.string.core_ui_mode_on_device_offline), PillTone.Accent, PesaIcons.CpuFill)
    }
}

/** Privacy banner at the top of the conversation: what the active mode does with the question. */
@Composable
fun ModeBanner(mode: AssistantMode) {
    val colors = PesaLensTheme.colors
    val cloud = mode == AssistantMode.Cloud
    val title = stringResource(if (cloud) R.string.core_ui_banner_cloud_title else R.string.core_ui_banner_on_device_title)
    val body = stringResource(if (cloud) R.string.core_ui_banner_cloud_body else R.string.core_ui_banner_on_device_body)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = PesaLensTheme.shapes.tile,
        color = if (cloud) colors.surface else colors.tintSubtle,
        border = BorderStroke(1.dp, if (cloud) colors.border else colors.accent),
    ) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
            Icon(
                if (cloud) PesaIcons.Cloud else PesaIcons.CpuFill,
                contentDescription = null,
                tint = if (cloud) colors.text else colors.accentText,
                modifier = Modifier.padding(top = 1.dp).size(17.dp),
            )
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = colors.text, fontWeight = FontWeight.Medium)) { append(title) }
                    append(' ')
                    append(body)
                },
                style = PesaLensTheme.type.meta,
                color = colors.textMuted,
            )
        }
    }
}
