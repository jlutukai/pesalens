package lutukai.pesalens.feature.applock.impl.presentation.locked

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.BorderStroke
import lutukai.pesalens.core.designsystem.icon.PesaIcons
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.designsystem.theme.PesaSpacing
import lutukai.pesalens.core.navigation.LocalNavigator
import lutukai.pesalens.core.ui.mvi.ObserveAsEvents
import lutukai.pesalens.feature.applock.impl.R
import lutukai.pesalens.feature.applock.impl.biometric.BiometricLauncher
import lutukai.pesalens.feature.applock.impl.biometric.BiometricOutcome
import lutukai.pesalens.feature.applock.impl.presentation.common.CenteredColumn
import lutukai.pesalens.feature.applock.impl.presentation.common.PinLayout
import lutukai.pesalens.feature.applock.impl.presentation.common.TitleBlock
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LockedScreen(viewModel: LockedViewModel = koinViewModel()) {
    val navigator = LocalNavigator.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val activity = LocalActivity.current as? FragmentActivity
    val launcher = remember(activity) { activity?.let(::BiometricLauncher) }
    val promptTitle = stringResource(R.string.applock_prompt_unlock_title)
    LaunchedEffect(viewModel) { viewModel.onIntent(LockedIntent.Load) }
    ObserveAsEvents(viewModel.effects) { effect ->
        when (effect) {
            LockedEffect.LaunchBiometricUnlock ->
                if (launcher != null && launcher.canUnlock()) {
                    launcher.launchUnlock(promptTitle) { viewModel.onIntent(LockedIntent.BiometricResult(it)) }
                } else {
                    viewModel.onIntent(LockedIntent.BiometricResult(BiometricOutcome.NotAvailable))
                }
            LockedEffect.Unlocked -> navigator.back()
        }
    }
    LockedContent(state = state, onIntent = viewModel::onIntent)
}

private val RingSize = 52.dp
private val RingDot = 18.dp
private val BioButtonSize = 96.dp

@Composable
fun LockedContent(state: LockedUiState, onIntent: (LockedIntent) -> Unit, modifier: Modifier = Modifier) {
    // The lock cannot be dismissed with the back gesture.
    BackHandler(enabled = true) {}
    Box(
        modifier
            .fillMaxSize()
            .background(PesaLensTheme.colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        CenteredColumn(Modifier.fillMaxSize()) {
            val padding = Modifier.fillMaxSize().padding(start = PesaSpacing.xxl, top = 56.dp, end = PesaSpacing.xxl, bottom = PesaSpacing.xl)
            when (state) {
                is LockedUiState.Pin -> PinVariant(state, onIntent, padding)
                is LockedUiState.Biometric -> BiometricVariant(state, onIntent, padding)
            }
        }
    }
}

@Composable
private fun BrandRing() {
    val colors = PesaLensTheme.colors
    Box(Modifier.size(RingSize).border(2.dp, colors.accent, CircleShape), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(RingDot)
                .background(colors.tint, CircleShape)
                .border(2.dp, colors.accentText, CircleShape),
        )
    }
}

@Composable
private fun PinVariant(state: LockedUiState.Pin, onIntent: (LockedIntent) -> Unit, modifier: Modifier) {
    val error = state.error?.let {
        when (it) {
            LockedError.Incorrect -> stringResource(R.string.applock_error_incorrect)
            is LockedError.CoolingDown -> stringResource(R.string.applock_error_cooldown, it.seconds)
            LockedError.BiometricUnavailable -> stringResource(R.string.applock_error_biometric_unavailable)
        }
    }
    PinLayout(
        digits = state.digits,
        error = error,
        onDigit = { onIntent(LockedIntent.Digit(it)) },
        onDelete = { onIntent(LockedIntent.Delete) },
        modifier = modifier,
    ) {
        BrandRing()
        TitleBlock(stringResource(R.string.applock_locked_title), stringResource(R.string.applock_locked_sub_pin))
    }
}

@Composable
private fun BiometricVariant(state: LockedUiState.Biometric, onIntent: (LockedIntent) -> Unit, modifier: Modifier) {
    val colors = PesaLensTheme.colors
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        BrandRing()
        TitleBlock(stringResource(R.string.applock_locked_title), stringResource(R.string.applock_locked_sub_biometric))
        Column(
            Modifier.weight(1f).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
        ) {
            val description = stringResource(R.string.applock_unlock_biometric_description)
            Surface(
                onClick = { onIntent(LockedIntent.UnlockWithBiometric) },
                modifier = Modifier.size(BioButtonSize).semantics { contentDescription = description },
                shape = CircleShape,
                color = colors.tintSubtle,
                contentColor = colors.accentText,
                border = BorderStroke(1.5.dp, colors.accent),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(PesaIcons.Fingerprint, contentDescription = null, modifier = Modifier.size(48.dp))
                }
            }
            Text(stringResource(R.string.applock_tap_to_unlock), style = PesaLensTheme.type.bodySmall, color = colors.textMuted)
            if (state.error != null) {
                Text(
                    stringResource(R.string.applock_error_biometric_unavailable),
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    style = PesaLensTheme.type.meta,
                    color = colors.warnText,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
