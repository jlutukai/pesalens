package lutukai.pesalens.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import lutukai.pesalens.core.designsystem.icon.PesaIcons
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.designsystem.theme.PesaSpacing

/** 64dp top bar (backContentDescription is mandatory when onBack is set) with a bottom 1dp border, optional 48dp back button, title, subtitle and trailing actions. */
@Composable
fun PesaTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    backContentDescription: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    require(onBack == null || backContentDescription != null) { "backContentDescription is required when onBack is set" }
    val colors = PesaLensTheme.colors
    val borderColor = colors.border
    Row(
        modifier
            .fillMaxWidth()
            .height(64.dp)
            .drawBehind {
                val w = 1.dp.toPx()
                drawLine(borderColor, Offset(0f, size.height - w / 2), Offset(size.width, size.height - w / 2), w)
            }
            .padding(start = if (onBack != null) PesaSpacing.xs else PesaSpacing.xl, end = PesaSpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PesaSpacing.xs),
    ) {
        if (onBack != null) {
            Box(
                Modifier
                    .size(PesaSpacing.minTouchTarget)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onBack)
                    .semantics { backContentDescription?.let { contentDescription = it } },
                contentAlignment = Alignment.Center,
            ) {
                Icon(PesaIcons.ArrowLeft, contentDescription = null, tint = colors.text, modifier = Modifier.size(22.dp))
            }
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = PesaLensTheme.type.titleSmall, color = colors.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) {
                Text(subtitle, style = PesaLensTheme.type.kicker.copy(letterSpacing = TextUnit.Unspecified), color = colors.textMuted)
            }
        }
        actions()
    }
}
