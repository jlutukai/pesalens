package lutukai.pesalens.feature.modelsetup.impl.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lutukai.pesalens.core.designsystem.adaptive.PesaWidthClass
import lutukai.pesalens.core.designsystem.adaptive.rememberPesaWindowInfo
import lutukai.pesalens.core.designsystem.component.CalloutTone
import lutukai.pesalens.core.designsystem.component.PesaButton
import lutukai.pesalens.core.designsystem.component.PesaButtonStyle
import lutukai.pesalens.core.designsystem.component.PesaCallout
import lutukai.pesalens.core.designsystem.component.PesaDownloadProgress
import lutukai.pesalens.core.designsystem.component.PesaTopBar
import lutukai.pesalens.core.designsystem.icon.PesaIcons
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.designsystem.theme.PesaSpacing
import lutukai.pesalens.core.model.ModelStatus
import lutukai.pesalens.core.navigation.LocalNavigator
import lutukai.pesalens.core.ui.catalog.ModelFileFacts
import lutukai.pesalens.core.ui.catalog.ModelStatusHeader
import lutukai.pesalens.core.ui.mvi.ObserveAsEvents
import lutukai.pesalens.feature.applock.api.AppLockSetup
import lutukai.pesalens.feature.applock.api.LockSetupOrigin
import lutukai.pesalens.feature.modelsetup.api.ModelSetupOrigin
import lutukai.pesalens.feature.modelsetup.impl.R
import lutukai.pesalens.feature.onboarding.api.CloudConsent
import lutukai.pesalens.feature.onboarding.api.ConsentOrigin
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Content never grows wider than this; on wide panes it is centred. */
private val ContentMaxWidth = 640.dp

@Composable
fun ModelSetupScreen(
    origin: ModelSetupOrigin,
    viewModel: ModelSetupViewModel = koinViewModel { parametersOf(origin) },
) {
    val navigator = LocalNavigator.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.onIntent(ModelSetupIntent.Load) }
    ObserveAsEvents(viewModel.effects) { effect ->
        when (effect) {
            ModelSetupEffect.Close -> navigator.back()
            ModelSetupEffect.OpenCloudConsent -> navigator.navigate(CloudConsent(ConsentOrigin.ModelSetup))
            ModelSetupEffect.ContinueOnboarding -> navigator.replaceAll(AppLockSetup(LockSetupOrigin.Onboarding))
        }
    }
    val onIntent = remember(viewModel) { viewModel::onIntent }
    ModelSetupContent(state = state, onIntent = onIntent)
}

@Composable
fun ModelSetupContent(state: ModelSetupUiState, onIntent: (ModelSetupIntent) -> Unit, modifier: Modifier = Modifier) {
    val gutter = if (rememberPesaWindowInfo().widthClass == PesaWidthClass.Compact) PesaSpacing.xl else PesaSpacing.xxl
    Column(
        modifier
            .fillMaxSize()
            .background(PesaLensTheme.colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        PesaTopBar(
            title = stringResource(R.string.model_title),
            onBack = { onIntent(ModelSetupIntent.Back) },
            backContentDescription = stringResource(R.string.model_back),
        )
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = gutter, top = PesaSpacing.xl, end = gutter, bottom = PesaSpacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                Modifier.widthIn(max = ContentMaxWidth).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                ModelStatusHeader(state.toStatus())
                when (state) {
                    is ModelSetupUiState.NotDownloaded -> NotDownloaded(onIntent)
                    is ModelSetupUiState.ReadyToDownload -> ReadyToDownload(onIntent)
                    is ModelSetupUiState.Downloading -> Progress(state.percent, verifying = false, onIntent)
                    is ModelSetupUiState.Verifying -> Progress(FullPercent, verifying = true, onIntent)
                    is ModelSetupUiState.Ready -> Ready(onIntent)
                    is ModelSetupUiState.Failed -> Failed(state, onIntent)
                    is ModelSetupUiState.Unsupported -> Unsupported(onIntent)
                }
            }
        }
    }
}

private const val FullPercent = 100

private fun ModelSetupUiState.toStatus(): ModelStatus = when (this) {
    is ModelSetupUiState.NotDownloaded -> ModelStatus.NotDownloaded
    is ModelSetupUiState.ReadyToDownload -> ModelStatus.ReadyToDownload
    is ModelSetupUiState.Downloading -> ModelStatus.Downloading(percent)
    is ModelSetupUiState.Verifying -> ModelStatus.Verifying
    is ModelSetupUiState.Ready -> ModelStatus.Ready
    is ModelSetupUiState.Failed -> ModelStatus.Failed(stoppedAtPercent)
    is ModelSetupUiState.Unsupported -> ModelStatus.Unsupported
}

@Composable
private fun Actions(spacing: Dp = PesaSpacing.sm + PesaSpacing.xxs, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(spacing)) { content() }
}

@Composable
private fun ActionButton(
    @StringRes text: Int,
    intent: ModelSetupIntent,
    onIntent: (ModelSetupIntent) -> Unit,
    style: PesaButtonStyle,
    leadingIcon: ImageVector? = null,
) {
    PesaButton(
        text = stringResource(text),
        onClick = { onIntent(intent) },
        modifier = Modifier.fillMaxWidth(),
        style = style,
        leadingIcon = leadingIcon,
    )
}

@Composable
private fun NotDownloaded(onIntent: (ModelSetupIntent) -> Unit) {
    Actions(spacing = 14.dp) {
        Text(stringResource(R.string.model_not_downloaded_intro), style = PesaLensTheme.type.body, color = PesaLensTheme.colors.text)
        Surface(
            Modifier.fillMaxWidth(),
            shape = PesaLensTheme.shapes.tileLarge,
            color = PesaLensTheme.colors.surface,
            contentColor = PesaLensTheme.colors.text,
            border = BorderStroke(1.dp, PesaLensTheme.colors.border),
        ) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Fact(PesaIcons.DeviceMobile, R.string.model_fact_runs_here)
                Fact(PesaIcons.WifiSlash, R.string.model_fact_offline)
                Fact(PesaIcons.Gauge, R.string.model_fact_compatibility)
                Fact(PesaIcons.HardDrives, R.string.model_fact_storage)
            }
        }
        ActionButton(R.string.model_check_device, ModelSetupIntent.CheckDevice, onIntent, PesaButtonStyle.Outlined)
        ActionButton(R.string.model_use_cloud_instead, ModelSetupIntent.UseCloud, onIntent, PesaButtonStyle.Text)
    }
}

@Composable
private fun Fact(icon: ImageVector, @StringRes text: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(icon, contentDescription = null, tint = PesaLensTheme.colors.accentText, modifier = Modifier.size(18.dp))
        Text(stringResource(text), style = PesaLensTheme.type.bodySmall, color = PesaLensTheme.colors.text)
    }
}

@Composable
private fun ReadyToDownload(onIntent: (ModelSetupIntent) -> Unit) {
    ModelFileFacts()
    Actions {
        Text(stringResource(R.string.model_ready_to_download_body), style = MutedBody, color = PesaLensTheme.colors.textMuted)
        ActionButton(R.string.model_download, ModelSetupIntent.Download, onIntent, PesaButtonStyle.Outlined, PesaIcons.DownloadSimple)
        ActionButton(R.string.model_not_now, ModelSetupIntent.Back, onIntent, PesaButtonStyle.Text)
    }
}

/** Download and verification share one block; only a running download can be cancelled. */
@Composable
private fun Progress(percent: Int, verifying: Boolean, onIntent: (ModelSetupIntent) -> Unit) {
    val colors = PesaLensTheme.colors
    ModelFileFacts()
    Actions {
        PesaDownloadProgress(percent, stringResource(R.string.model_progress_description))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                stringResource(if (verifying) R.string.model_progress_verifying else R.string.model_progress_downloading),
                modifier = Modifier.weight(1f),
                style = PesaLensTheme.type.bodySmall,
                color = colors.text,
            )
            Text(
                stringResource(R.string.model_progress_percent, percent),
                style = PesaLensTheme.type.bodySmall.copy(fontWeight = FontWeight.Medium, fontFeatureSettings = "tnum"),
                color = colors.text,
            )
        }
        Text(stringResource(R.string.model_progress_body), style = PesaLensTheme.type.meta.copy(lineHeight = 19.5.sp), color = colors.textMuted)
        if (!verifying) ActionButton(R.string.model_cancel_download, ModelSetupIntent.Cancel, onIntent, PesaButtonStyle.Neutral)
    }
}

@Composable
private fun Ready(onIntent: (ModelSetupIntent) -> Unit) {
    Actions {
        Text(stringResource(R.string.model_ready_body), style = PesaLensTheme.type.body, color = PesaLensTheme.colors.text)
        ActionButton(R.string.model_switch_on_device, ModelSetupIntent.UseOnDevice, onIntent, PesaButtonStyle.Outlined)
        ActionButton(R.string.model_delete, ModelSetupIntent.Delete, onIntent, PesaButtonStyle.Text, PesaIcons.Trash)
    }
}

@Composable
private fun Failed(state: ModelSetupUiState.Failed, onIntent: (ModelSetupIntent) -> Unit) {
    ModelFileFacts()
    Actions {
        PesaCallout(
            text = stringResource(R.string.model_failed_alert, state.stoppedAtPercent),
            icon = PesaIcons.WifiSlash,
            tone = CalloutTone.Warn,
            liveRegion = true,
        )
        Text(stringResource(R.string.model_failed_body), style = PesaLensTheme.type.meta.copy(lineHeight = 19.5.sp), color = PesaLensTheme.colors.textMuted)
        ActionButton(R.string.model_retry, ModelSetupIntent.Retry, onIntent, PesaButtonStyle.Outlined)
        ActionButton(R.string.model_use_cloud, ModelSetupIntent.UseCloud, onIntent, PesaButtonStyle.Text)
    }
}

@Composable
private fun Unsupported(onIntent: (ModelSetupIntent) -> Unit) {
    Actions {
        Text(stringResource(R.string.model_unsupported_body), style = PesaLensTheme.type.body, color = PesaLensTheme.colors.text)
        Text(stringResource(R.string.model_unsupported_note), style = PesaLensTheme.type.meta.copy(lineHeight = 19.5.sp), color = PesaLensTheme.colors.textMuted)
        ActionButton(R.string.model_continue_cloud, ModelSetupIntent.UseCloud, onIntent, PesaButtonStyle.Outlined)
    }
}

private val MutedBody @Composable get() = PesaLensTheme.type.bodySmall.copy(lineHeight = 21.sp)
