package lutukai.pesalens.feature.settings.impl.presentation

import android.graphics.Typeface
import android.text.Spanned
import android.text.style.StyleSpan
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import lutukai.pesalens.core.designsystem.adaptive.PesaWidthClass
import lutukai.pesalens.core.designsystem.adaptive.rememberPesaWindowInfo
import lutukai.pesalens.core.designsystem.component.PesaAlertDialog
import lutukai.pesalens.core.designsystem.component.PesaButton
import lutukai.pesalens.core.designsystem.component.PesaButtonStyle
import lutukai.pesalens.core.designsystem.component.PesaCardRow
import lutukai.pesalens.core.designsystem.component.PesaExpandable
import lutukai.pesalens.core.designsystem.component.PesaNavRow
import lutukai.pesalens.core.designsystem.component.PesaSegmentedControl
import lutukai.pesalens.core.designsystem.component.PesaTopBar
import lutukai.pesalens.core.designsystem.component.SegmentOption
import lutukai.pesalens.core.designsystem.icon.PesaIcons
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.designsystem.theme.PesaSpacing
import lutukai.pesalens.core.model.AssistantMode
import lutukai.pesalens.core.model.LockMethod
import lutukai.pesalens.core.model.ModelStatus
import lutukai.pesalens.core.navigation.LocalNavigator
import lutukai.pesalens.core.ui.mvi.ObserveAsEvents
import lutukai.pesalens.feature.applock.api.AppLockSetup
import lutukai.pesalens.feature.applock.api.LockSetupOrigin
import lutukai.pesalens.feature.modelsetup.api.ModelSetup
import lutukai.pesalens.feature.modelsetup.api.ModelSetupOrigin
import lutukai.pesalens.feature.onboarding.api.CloudConsent
import lutukai.pesalens.feature.onboarding.api.ConsentOrigin
import lutukai.pesalens.feature.settings.impl.R
import org.koin.compose.viewmodel.koinViewModel

/** Content never grows wider than this; on wide panes it is centred. */
private val ContentMaxWidth = 640.dp

/** Two icon-and-label segments; the equal-width segments need a bounded width beside the "Theme" label. */
private val ThemeControlWidth = 220.dp

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = koinViewModel()) {
    val navigator = LocalNavigator.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val systemDark = isSystemInDarkTheme()
    LaunchedEffect(viewModel, systemDark) { viewModel.onIntent(SettingsIntent.Load(systemDark)) }
    ObserveAsEvents(viewModel.effects) { effect ->
        when (effect) {
            SettingsEffect.OpenModel -> navigator.navigate(ModelSetup(ModelSetupOrigin.Settings))
            SettingsEffect.OpenAppLock -> navigator.navigate(AppLockSetup(LockSetupOrigin.Settings))
            SettingsEffect.OpenCloudConsent -> navigator.navigate(CloudConsent(ConsentOrigin.Settings))
            SettingsEffect.Close -> navigator.back()
        }
    }
    val onIntent = remember(viewModel) { viewModel::onIntent }
    SettingsContent(state = state, onIntent = onIntent)
}

@Composable
fun SettingsContent(state: SettingsUiState, onIntent: (SettingsIntent) -> Unit, modifier: Modifier = Modifier) {
    val gutter = if (rememberPesaWindowInfo().widthClass == PesaWidthClass.Compact) PesaSpacing.lg else PesaSpacing.xxl
    Column(
        modifier
            .fillMaxSize()
            .background(PesaLensTheme.colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        PesaTopBar(
            title = stringResource(R.string.settings_title),
            onBack = { onIntent(SettingsIntent.Back) },
            backContentDescription = stringResource(R.string.settings_back),
        )
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = gutter, top = PesaSpacing.md, end = gutter, bottom = PesaSpacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                Modifier.widthIn(max = ContentMaxWidth).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(PesaSpacing.xl),
            ) {
                ModeSection(state, onIntent)
                PesaCardRow(
                    title = stringResource(R.string.settings_model_title),
                    subtitle = modelSubtitle(state.modelStatus),
                    icon = PesaIcons.Cpu,
                    onClick = { onIntent(SettingsIntent.OpenModel) },
                )
                SecurityAndAppearance(state, onIntent)
                DataSection(onIntent)
            }
        }
    }
    when (state.dialog) {
        SettingsDialog.None -> Unit
        SettingsDialog.ClearConversation -> ClearDialog(onIntent)
        SettingsDialog.ResetSampleData -> ResetDialog(onIntent)
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier,
        style = PesaLensTheme.type.meta.copy(fontWeight = FontWeight.Medium),
        color = PesaLensTheme.colors.textMuted,
    )
}

@Composable
private fun ModeSection(state: SettingsUiState, onIntent: (SettingsIntent) -> Unit) {
    val options = persistentListOf(
        SegmentOption(AssistantMode.Cloud, stringResource(R.string.settings_mode_cloud), PesaIcons.Cloud),
        SegmentOption(AssistantMode.OnDevice, stringResource(R.string.settings_mode_on_device), PesaIcons.Cpu, enabled = state.onDeviceEnabled),
    )
    val help = when (state.mode) {
        AssistantMode.OnDevice -> R.string.settings_mode_help_on_device
        AssistantMode.Cloud -> if (state.onDeviceEnabled) R.string.settings_mode_help_cloud_ready else R.string.settings_mode_help_cloud_setup
    }
    Column(verticalArrangement = Arrangement.spacedBy(PesaSpacing.sm)) {
        SectionLabel(stringResource(R.string.settings_mode_label))
        PesaSegmentedControl(
            options = options,
            selected = state.mode,
            onSelect = { onIntent(SettingsIntent.SetMode(it)) },
            contentDescription = stringResource(R.string.settings_mode_label),
            modifier = Modifier.fillMaxWidth(),
        )
        Text(stringResource(help), style = PesaLensTheme.type.meta, color = PesaLensTheme.colors.textMuted)
    }
}

@Composable
private fun SecurityAndAppearance(state: SettingsUiState, onIntent: (SettingsIntent) -> Unit) {
    val lockStatus = stringResource(
        when (state.lockMethod) {
            LockMethod.None -> R.string.settings_lock_off
            LockMethod.Pin -> R.string.settings_lock_pin
            LockMethod.Biometric -> R.string.settings_lock_biometric
        },
    )
    val themeOptions = persistentListOf(
        SegmentOption(false, stringResource(R.string.settings_theme_light), PesaIcons.Sun),
        SegmentOption(true, stringResource(R.string.settings_theme_dark), PesaIcons.Moon),
    )
    Column {
        SectionLabel(stringResource(R.string.settings_security_header), Modifier.padding(bottom = PesaSpacing.xs))
        PesaNavRow(
            title = stringResource(R.string.settings_app_lock),
            icon = PesaIcons.LockKey,
            onClick = { onIntent(SettingsIntent.OpenAppLock) },
            subtitle = lockStatus,
        )
        SectionLabel(stringResource(R.string.settings_appearance_header), Modifier.padding(top = PesaSpacing.lg, bottom = PesaSpacing.xs))
        Row(
            Modifier.fillMaxWidth().heightIn(min = 56.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PesaSpacing.md),
        ) {
            Text(
                stringResource(R.string.settings_theme),
                modifier = Modifier.weight(1f).padding(start = PesaSpacing.xs),
                style = PesaLensTheme.type.body,
                color = PesaLensTheme.colors.text,
            )
            PesaSegmentedControl(
                options = themeOptions,
                selected = state.resolvedDark,
                onSelect = { onIntent(SettingsIntent.SetTheme(it)) },
                contentDescription = stringResource(R.string.settings_theme),
                modifier = Modifier.width(ThemeControlWidth),
            )
        }
    }
}

@Composable
private fun DataSection(onIntent: (SettingsIntent) -> Unit) {
    Column {
        SectionLabel(stringResource(R.string.settings_data_header), Modifier.padding(bottom = PesaSpacing.xs))
        PesaNavRow(
            title = stringResource(R.string.settings_clear_conversation),
            icon = PesaIcons.Broom,
            onClick = { onIntent(SettingsIntent.AskClear) },
            showChevron = false,
            topBorder = true,
        )
        PesaNavRow(
            title = stringResource(R.string.settings_reset_sample_data),
            icon = PesaIcons.ArrowCounterClockwise,
            onClick = { onIntent(SettingsIntent.AskReset) },
            subtitle = stringResource(R.string.settings_reset_sample_data_subtitle),
            showChevron = false,
            topBorder = true,
        )
        PesaExpandable(stringResource(R.string.settings_privacy_title), PesaIcons.ShieldCheck) {
            ExpandedText(verticalGap = PesaSpacing.xs + PesaSpacing.xxs) {
                Text(stringResource(R.string.settings_privacy_local))
                Text(styledString(R.string.settings_privacy_cloud))
                Text(styledString(R.string.settings_privacy_on_device))
            }
        }
        PesaExpandable(stringResource(R.string.settings_about_title), PesaIcons.Info) {
            ExpandedText { Text(stringResource(R.string.settings_about_body)) }
        }
    }
}

/** Indented 13sp muted body under an expandable row. Strong runs inside use the text colour at weight 500. */
@Composable
private fun ExpandedText(verticalGap: Dp = 0.dp, content: @Composable () -> Unit) {
    val style = PesaLensTheme.type.meta.copy(lineHeight = 19.5.sp)
    CompositionLocalProvider(
        LocalTextStyle provides style,
        LocalContentColor provides PesaLensTheme.colors.textMuted,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(start = ExpandedIndent, end = PesaSpacing.xs, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(verticalGap),
        ) { content() }
    }
}

/** Row padding 4dp + icon 20dp + gap 14dp: the text lines up under the row title. */
private val ExpandedIndent = 38.dp

@Composable
private fun modelSubtitle(status: ModelStatus): String = when (status) {
    ModelStatus.NotDownloaded -> stringResource(R.string.settings_model_not_downloaded)
    ModelStatus.ReadyToDownload -> stringResource(R.string.settings_model_ready_to_download)
    is ModelStatus.Downloading -> stringResource(R.string.settings_model_downloading, status.percent)
    ModelStatus.Verifying -> stringResource(R.string.settings_model_verifying)
    ModelStatus.Ready -> stringResource(R.string.settings_model_ready)
    is ModelStatus.Failed -> stringResource(R.string.settings_model_failed)
    ModelStatus.Unsupported -> stringResource(R.string.settings_model_unsupported)
}

@Composable
private fun ClearDialog(onIntent: (SettingsIntent) -> Unit) {
    PesaAlertDialog(
        title = stringResource(R.string.settings_clear_dialog_title),
        onDismiss = { onIntent(SettingsIntent.Dismiss) },
        subtitle = stringResource(R.string.settings_clear_dialog_body),
        dismiss = {
            PesaButton(stringResource(R.string.settings_clear_dialog_cancel), { onIntent(SettingsIntent.Dismiss) }, style = PesaButtonStyle.Text)
        },
        confirm = {
            PesaButton(stringResource(R.string.settings_clear_dialog_confirm), { onIntent(SettingsIntent.ConfirmClear) }, style = PesaButtonStyle.Outlined)
        },
    )
}

@Composable
private fun ResetDialog(onIntent: (SettingsIntent) -> Unit) {
    PesaAlertDialog(
        title = stringResource(R.string.settings_reset_dialog_title),
        onDismiss = { onIntent(SettingsIntent.Dismiss) },
        icon = PesaIcons.ArrowCounterClockwise,
        iconTint = PesaLensTheme.colors.warnText,
        dismiss = {
            PesaButton(stringResource(R.string.settings_reset_dialog_cancel), { onIntent(SettingsIntent.Dismiss) }, style = PesaButtonStyle.Text)
        },
        confirm = {
            PesaButton(stringResource(R.string.settings_reset_dialog_confirm), { onIntent(SettingsIntent.ConfirmReset) }, style = PesaButtonStyle.Warn)
        },
    ) {
        Text(
            styledString(R.string.settings_reset_dialog_body),
            style = PesaLensTheme.type.bodySmall.copy(lineHeight = 21.sp),
            color = PesaLensTheme.colors.textMuted,
        )
    }
}

/**
 * A string resource with `<b>` runs rendered in the text colour at the theme's medium weight (the prototype's
 * `<strong style="color:var(--tx);font-weight:500">`), not bold.
 */
@Composable
private fun styledString(@StringRes id: Int): AnnotatedString {
    val resources = LocalResources.current
    val strong = PesaLensTheme.colors.text
    return remember(id, resources, strong) {
        val text = resources.getText(id)
        buildAnnotatedString {
            append(text.toString())
            (text as? Spanned)?.getSpans(0, text.length, StyleSpan::class.java)?.forEach { span ->
                if (span.style == Typeface.BOLD) {
                    addStyle(
                        SpanStyle(fontWeight = FontWeight.Medium, color = strong),
                        text.getSpanStart(span),
                        text.getSpanEnd(span),
                    )
                }
            }
        }
    }
}
