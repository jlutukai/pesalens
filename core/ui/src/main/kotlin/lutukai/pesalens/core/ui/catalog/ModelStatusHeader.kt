package lutukai.pesalens.core.ui.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import lutukai.pesalens.core.designsystem.component.PesaIconTile
import lutukai.pesalens.core.designsystem.component.PesaKicker
import lutukai.pesalens.core.designsystem.component.TileTone
import lutukai.pesalens.core.designsystem.icon.PesaIcons
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.model.ModelStatus
import lutukai.pesalens.core.ui.R

/** 52dp status tile plus "Model status" kicker and the status title. Failed and unsupported use the warn tone. */
@Composable
fun ModelStatusHeader(status: ModelStatus, modifier: Modifier = Modifier) {
    val warn = status is ModelStatus.Failed || status is ModelStatus.Unsupported
    Row(
        modifier.semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PesaIconTile(status.icon(), size = 52.dp, iconSize = 26.dp, tone = if (warn) TileTone.Warn else TileTone.Accent, shape = PesaLensTheme.shapes.tileLarge)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            PesaKicker(stringResource(R.string.core_ui_model_status_kicker))
            Text(stringResource(status.titleRes()), style = PesaLensTheme.type.titleMedium, color = PesaLensTheme.colors.text)
        }
    }
}

private fun ModelStatus.icon(): ImageVector = when (this) {
    ModelStatus.NotDownloaded -> PesaIcons.Cpu
    ModelStatus.ReadyToDownload -> PesaIcons.DownloadSimple
    is ModelStatus.Downloading -> PesaIcons.DownloadSimple
    ModelStatus.Verifying -> PesaIcons.ShieldCheck
    ModelStatus.Ready -> PesaIcons.CheckCircleFill
    is ModelStatus.Failed -> PesaIcons.WarningCircle
    ModelStatus.Unsupported -> PesaIcons.DeviceMobileSlash
}

private fun ModelStatus.titleRes(): Int = when (this) {
    ModelStatus.NotDownloaded -> R.string.core_ui_model_status_not_downloaded
    ModelStatus.ReadyToDownload -> R.string.core_ui_model_status_ready_to_download
    is ModelStatus.Downloading -> R.string.core_ui_model_status_downloading
    ModelStatus.Verifying -> R.string.core_ui_model_status_verifying
    ModelStatus.Ready -> R.string.core_ui_model_status_ready
    is ModelStatus.Failed -> R.string.core_ui_model_status_failed
    ModelStatus.Unsupported -> R.string.core_ui_model_status_unsupported
}
