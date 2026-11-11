package lutukai.pesalens.feature.applock.impl.presentation.setup

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lutukai.pesalens.core.designsystem.component.PesaButton
import lutukai.pesalens.core.designsystem.component.PesaButtonSize
import lutukai.pesalens.core.designsystem.component.PesaButtonStyle
import lutukai.pesalens.core.designsystem.component.PesaCardRow
import lutukai.pesalens.core.designsystem.component.PesaIconTile
import lutukai.pesalens.core.designsystem.icon.PesaIcons
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.designsystem.theme.PesaSpacing
import lutukai.pesalens.core.model.LockMethod
import lutukai.pesalens.core.navigation.LocalNavigator
import lutukai.pesalens.core.ui.mvi.ObserveAsEvents
import lutukai.pesalens.feature.applock.api.AppLockSetup
import lutukai.pesalens.feature.applock.api.LockSetupOrigin
import lutukai.pesalens.feature.applock.impl.R
import lutukai.pesalens.feature.applock.impl.biometric.BiometricLauncher
import lutukai.pesalens.feature.applock.impl.biometric.BiometricOutcome
import lutukai.pesalens.feature.applock.impl.presentation.common.CenteredColumn
import lutukai.pesalens.feature.applock.impl.presentation.common.PinLayout
import lutukai.pesalens.feature.applock.impl.presentation.common.TitleBlock
import lutukai.pesalens.feature.assistant.api.Assistant
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AppLockSetupScreen(
    destination: AppLockSetup,
    viewModel: AppLockSetupViewModel = koinViewModel { parametersOf(destination.origin) },
) {
    val navigator = LocalNavigator.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val activity = LocalActivity.current as? FragmentActivity
    val launcher = remember(activity) { activity?.let(::BiometricLauncher) }
    val promptTitle = stringResource(R.string.applock_prompt_setup_title)
    val promptSubtitle = stringResource(R.string.applock_option_biometric_subtitle)
    val usePinLabel = stringResource(R.string.applock_prompt_use_pin)
    LaunchedEffect(viewModel) { viewModel.onIntent(AppLockSetupIntent.Load) }
    ObserveAsEvents(viewModel.effects) { effect ->
        when (effect) {
            AppLockSetupEffect.LaunchBiometricSetup ->
                if (launcher != null && launcher.canUseBiometricsForSetup()) {
                    launcher.launchSetup(promptTitle, promptSubtitle, usePinLabel) {
                        viewModel.onIntent(AppLockSetupIntent.BiometricResult(it))
                    }
                } else {
                    viewModel.onIntent(AppLockSetupIntent.BiometricResult(BiometricOutcome.NotAvailable))
                }
            // Beside Settings, back() would pop whatever is on top; this screen removes itself.
            AppLockSetupEffect.Finish -> when (destination.origin) {
                LockSetupOrigin.Onboarding -> navigator.replaceAll(Assistant)
                LockSetupOrigin.Settings -> navigator.remove(destination)
            }
            AppLockSetupEffect.Close -> navigator.remove(destination)
        }
    }
    AppLockSetupContent(state = state, onIntent = viewModel::onIntent)
}

private val TileSize = 56.dp
private val TileIconSize = 28.dp

@Composable
fun AppLockSetupContent(state: AppLockSetupUiState, onIntent: (AppLockSetupIntent) -> Unit, modifier: Modifier = Modifier) {
    val showBack = when (state) {
        is AppLockSetupUiState.Choose -> state.origin == LockSetupOrigin.Settings
        is AppLockSetupUiState.EnterPin -> true
    }
    // In the PIN step the system back returns to the choice, like the on-screen back button.
    BackHandler(enabled = state is AppLockSetupUiState.EnterPin) { onIntent(AppLockSetupIntent.Back) }
    Column(
        modifier
            .fillMaxSize()
            .background(PesaLensTheme.colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Row(Modifier.fillMaxWidth().height(56.dp).padding(horizontal = PesaSpacing.xs), verticalAlignment = Alignment.CenterVertically) {
            if (showBack) BackButton(onClick = { onIntent(AppLockSetupIntent.Back) })
        }
        CenteredColumn(Modifier.weight(1f).fillMaxWidth()) {
            when (state) {
                is AppLockSetupUiState.Choose -> ChooseBody(state, onIntent)
                is AppLockSetupUiState.EnterPin -> PinBody(state, onIntent)
            }
        }
    }
}

@Composable
private fun BackButton(onClick: () -> Unit) {
    val label = stringResource(R.string.applock_back)
    Box(
        Modifier
            .size(PesaSpacing.minTouchTarget)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Icon(PesaIcons.ArrowLeft, contentDescription = null, tint = PesaLensTheme.colors.text, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun ChooseBody(state: AppLockSetupUiState.Choose, onIntent: (AppLockSetupIntent) -> Unit) {
    val colors = PesaLensTheme.colors
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val minHeight = maxHeight
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = PesaSpacing.xxl, top = PesaSpacing.xs, end = PesaSpacing.xxl, bottom = PesaSpacing.xl),
        ) {
            Column(Modifier.fillMaxWidth().heightIn(min = minHeight - PesaSpacing.xs - PesaSpacing.xl)) {
                PesaIconTile(PesaIcons.LockKey, TileSize, TileIconSize, shape = PesaLensTheme.shapes.card)
                Text(
                    stringResource(R.string.applock_choose_title),
                    modifier = Modifier.padding(top = PesaSpacing.xl, bottom = PesaSpacing.sm),
                    style = PesaLensTheme.type.headline,
                    color = colors.text,
                )
                Text(
                    stringResource(R.string.applock_choose_body),
                    modifier = Modifier.padding(bottom = PesaSpacing.xxl),
                    style = PesaLensTheme.type.body,
                    color = colors.textMuted,
                )
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PesaCardRow(
                        title = stringResource(R.string.applock_option_biometric_title),
                        subtitle = stringResource(R.string.applock_option_biometric_subtitle),
                        icon = PesaIcons.Fingerprint,
                        onClick = { onIntent(AppLockSetupIntent.ChooseBiometric) },
                    )
                    PesaCardRow(
                        title = stringResource(R.string.applock_option_pin_title),
                        subtitle = stringResource(R.string.applock_option_pin_subtitle),
                        icon = PesaIcons.Password,
                        onClick = { onIntent(AppLockSetupIntent.ChoosePin) },
                    )
                    val status = when (state.currentMethod) {
                        LockMethod.None -> null
                        LockMethod.Pin -> stringResource(R.string.applock_status_pin)
                        LockMethod.Biometric -> stringResource(R.string.applock_status_biometric)
                    }
                    if (status != null) {
                        PesaCardRow(
                            title = stringResource(R.string.applock_option_off_title),
                            subtitle = stringResource(R.string.applock_option_off_subtitle, status),
                            icon = PesaIcons.LockOpen,
                            onClick = { onIntent(AppLockSetupIntent.TurnOff) },
                        )
                    }
                }
                Spacer(Modifier.weight(1f).heightIn(min = PesaSpacing.xl))
                PesaButton(
                    text = stringResource(
                        when (state.origin) {
                            LockSetupOrigin.Onboarding -> R.string.applock_skip
                            LockSetupOrigin.Settings -> R.string.applock_cancel
                        },
                    ),
                    onClick = { onIntent(AppLockSetupIntent.Skip) },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = PesaButtonStyle.TextMuted,
                    size = PesaButtonSize.Small,
                )
            }
        }
    }
}

@Composable
private fun PinBody(state: AppLockSetupUiState.EnterPin, onIntent: (AppLockSetupIntent) -> Unit) {
    val error = state.error?.let {
        stringResource(
            when (it) {
                PinError.Weak -> R.string.applock_error_weak
                PinError.Mismatch -> R.string.applock_error_mismatch
                PinError.Incorrect -> R.string.applock_error_incorrect
            },
        )
    }
    PinLayout(
        digits = state.digits,
        error = error,
        onDigit = { onIntent(AppLockSetupIntent.Digit(it)) },
        onDelete = { onIntent(AppLockSetupIntent.Delete) },
        modifier = Modifier
            .fillMaxSize()
            .padding(start = PesaSpacing.xxl, top = PesaSpacing.xs, end = PesaSpacing.xxl, bottom = PesaSpacing.xl),
    ) {
        PesaIconTile(PesaIcons.Password, TileSize, TileIconSize, shape = PesaLensTheme.shapes.card)
        when (state.step) {
            PinStep.Create -> TitleBlock(stringResource(R.string.applock_pin_create_title), stringResource(R.string.applock_pin_create_subtitle))
            PinStep.Confirm -> TitleBlock(stringResource(R.string.applock_pin_confirm_title), stringResource(R.string.applock_pin_confirm_subtitle))
        }
    }
}
