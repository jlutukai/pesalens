package lutukai.pesalens.core.ui.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import lutukai.pesalens.core.designsystem.component.PesaButton
import lutukai.pesalens.core.designsystem.component.PesaCard
import lutukai.pesalens.core.designsystem.component.PesaIconTile
import lutukai.pesalens.core.designsystem.component.TileTone
import lutukai.pesalens.core.designsystem.icon.PesaIcons
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.ui.catalog.model.NoticeActionUi
import lutukai.pesalens.core.ui.catalog.model.NoticeKind

private fun NoticeKind.icon(): ImageVector = when (this) {
    NoticeKind.NoData -> PesaIcons.CalendarX
    NoticeKind.Unsupported -> PesaIcons.Question
    NoticeKind.ModelUnavailable -> PesaIcons.Cpu
    NoticeKind.CloudOffline -> PesaIcons.WifiSlash
    NoticeKind.InvalidResponse -> PesaIcons.WarningCircle
    NoticeKind.SaveFailed -> PesaIcons.FloppyDisk
}

private fun NoticeKind.tone(): TileTone = when (this) {
    NoticeKind.ModelUnavailable, NoticeKind.CloudOffline, NoticeKind.InvalidResponse, NoticeKind.SaveFailed -> TileTone.Warn
    NoticeKind.NoData, NoticeKind.Unsupported -> TileTone.Accent
}

/**
 * Assistant notice card, announced politely. The caller supplies all copy; tapping an action reports its
 * [NoticeActionUi.onClickKey] through [onAction].
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatusNotice(
    kind: NoticeKind,
    title: String,
    body: String,
    actions: ImmutableList<NoticeActionUi>,
    onAction: (String) -> Unit,
) {
    val colors = PesaLensTheme.colors
    PesaCard(Modifier.semantics { liveRegion = LiveRegionMode.Polite }, spacing = 12.dp) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            PesaIconTile(kind.icon(), size = 40.dp, iconSize = 20.dp, tone = kind.tone())
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = PesaLensTheme.type.subtitle, color = colors.text)
                Text(body, style = PesaLensTheme.type.bodySmall, color = colors.textMuted)
            }
        }
        if (actions.isNotEmpty()) {
            FlowRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                actions.forEach { PesaButton(it.label, onClick = { onAction(it.onClickKey) }) }
            }
        }
    }
}
