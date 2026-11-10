package lutukai.pesalens.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.designsystem.theme.PesaSpacing

private val DialogMaxWidth = 560.dp

/**
 * Alert dialog on M3 [BasicAlertDialog]: dialog shape, 22dp padding, 14dp gap, max 560dp wide, on the scrim token.
 * Elevation is a 1dp border plus M3 shadow elevation (the prototype's `--shd`: hairline edge and soft ambient shadow).
 */
@Composable
fun PesaAlertDialog(
    title: String,
    onDismiss: () -> Unit,
    confirm: @Composable () -> Unit,
    dismiss: @Composable () -> Unit,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        // The scrim is drawn by the content so the window's default dim is zeroed to avoid doubling it.
        val view = LocalView.current
        SideEffect { (view.parent as? DialogWindowProvider)?.window?.setDimAmount(0f) }
        PesaAlertDialogContent(title, onDismiss, confirm, dismiss, icon, iconTint, subtitle, content)
    }
}

@Composable
internal fun PesaAlertDialogContent(
    title: String,
    onDismiss: () -> Unit,
    confirm: @Composable () -> Unit,
    dismiss: @Composable () -> Unit,
    icon: ImageVector?,
    iconTint: Color?,
    subtitle: String?,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = PesaLensTheme.colors
    val type = PesaLensTheme.type
    Box(
        Modifier
            .fillMaxSize()
            .background(colors.scrim)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss)
            .padding(PesaSpacing.xl),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = DialogMaxWidth)
                .fillMaxWidth()
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = {})
                .semantics { paneTitle = title },
            shape = PesaLensTheme.shapes.dialog,
            color = colors.surface,
            contentColor = colors.text,
            border = BorderStroke(1.dp, colors.border),
            shadowElevation = 16.dp,
        ) {
            Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = iconTint ?: colors.accentText, modifier = Modifier.size(26.dp))
                }
                Text(title, style = type.titleMedium, color = colors.text)
                if (subtitle != null) Text(subtitle, style = type.bodySmall.copy(lineHeight = 21.sp), color = colors.textMuted)
                content()
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(PesaSpacing.sm, Alignment.End)) {
                    dismiss()
                    confirm()
                }
            }
        }
    }
}
