package lutukai.pesalens.feature.onboarding.impl.presentation.welcome

import androidx.compose.foundation.border
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lutukai.pesalens.core.designsystem.adaptive.PesaWidthClass
import lutukai.pesalens.core.designsystem.adaptive.rememberPesaWindowInfo
import lutukai.pesalens.core.designsystem.component.PesaButton
import lutukai.pesalens.core.designsystem.component.PesaButtonStyle
import lutukai.pesalens.core.designsystem.pattern.ChartPattern
import lutukai.pesalens.core.designsystem.pattern.patternFill
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.designsystem.theme.PesaSpacing
import lutukai.pesalens.core.navigation.LocalNavigator
import lutukai.pesalens.core.ui.mvi.ObserveAsEvents
import lutukai.pesalens.feature.applock.api.AppLockSetup
import lutukai.pesalens.feature.applock.api.LockSetupOrigin
import lutukai.pesalens.feature.modelsetup.api.ModelSetup
import lutukai.pesalens.feature.modelsetup.api.ModelSetupOrigin
import lutukai.pesalens.feature.onboarding.api.CloudConsent
import lutukai.pesalens.feature.onboarding.api.ConsentOrigin
import lutukai.pesalens.feature.onboarding.impl.R
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WelcomeScreen(viewModel: WelcomeViewModel = koinViewModel()) {
    val navigator = LocalNavigator.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.onIntent(WelcomeIntent.Load) }
    ObserveAsEvents(viewModel.effects) { effect ->
        when (effect) {
            WelcomeEffect.OpenConsent -> navigator.navigate(CloudConsent(ConsentOrigin.Welcome))
            WelcomeEffect.OpenAppLockSetup -> navigator.replaceAll(AppLockSetup(LockSetupOrigin.Onboarding))
            WelcomeEffect.OpenModelSetup -> navigator.navigate(ModelSetup(ModelSetupOrigin.Welcome))
        }
    }
    WelcomeContent(state = state, onIntent = viewModel::onIntent)
}

private val ContentMaxWidth = 960.dp
private val BrandRing = 34.dp
private val BrandDot = 12.dp
private val BarHeight = 22.dp
private val BarWeights = listOf(25f, 15f, 8f, 5f, 7f, 5f)

@Composable
fun WelcomeContent(state: WelcomeUiState, onIntent: (WelcomeIntent) -> Unit, modifier: Modifier = Modifier) {
    val window = rememberPesaWindowInfo()
    val twoColumns = window.widthClass == PesaWidthClass.Expanded || window.isCompactHeight
    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .background(PesaLensTheme.colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        val minHeight = maxHeight
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = PesaSpacing.xxl, top = PesaSpacing.xl, end = PesaSpacing.xxl, bottom = PesaSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                Modifier
                    .widthIn(max = ContentMaxWidth)
                    .fillMaxWidth()
                    .heightIn(min = minHeight - PesaSpacing.xl * 2),
            ) {
                BrandRow()
                if (twoColumns) {
                    Row(
                        Modifier.weight(1f).fillMaxWidth().padding(top = PesaSpacing.xl),
                        horizontalArrangement = Arrangement.spacedBy(PesaSpacing.xxxl),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) { Copy(onIntent) }
                        PreviewCard(Modifier.weight(1f))
                    }
                } else {
                    Spacer(Modifier.weight(1f).heightIn(min = PesaSpacing.xxl))
                    PreviewCard(Modifier.fillMaxWidth())
                    Spacer(Modifier.height(28.dp))
                    Copy(onIntent)
                }
            }
        }
    }
}

@Composable
private fun BrandRow() {
    val colors = PesaLensTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            Modifier.size(BrandRing).border(2.dp, colors.accent, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(BrandDot)
                    .background(colors.tint, CircleShape)
                    .border(2.dp, colors.accentText, CircleShape),
            )
        }
        Text(stringResource(R.string.welcome_brand), style = PesaLensTheme.type.titleMedium, color = colors.text)
    }
}

/** Headline, body and the two buttons. The prototype's 28dp headline top margin is the caller's gap in compact. */
@Composable
private fun Copy(onIntent: (WelcomeIntent) -> Unit) {
    val colors = PesaLensTheme.colors
    Column {
        Text(stringResource(R.string.welcome_headline), style = PesaLensTheme.type.display, color = colors.text)
        Spacer(Modifier.height(PesaSpacing.md))
        Text(stringResource(R.string.welcome_body), style = PesaLensTheme.type.body, color = colors.textMuted)
        Spacer(Modifier.height(28.dp))
        Column(verticalArrangement = Arrangement.spacedBy(PesaSpacing.xs)) {
            PesaButton(
                text = stringResource(R.string.welcome_get_started),
                onClick = { onIntent(WelcomeIntent.GetStarted) },
                modifier = Modifier.fillMaxWidth(),
                style = PesaButtonStyle.Tonal,
                large = true,
            )
            PesaButton(
                text = stringResource(R.string.welcome_use_on_device),
                onClick = { onIntent(WelcomeIntent.UseOnDeviceInstead) },
                modifier = Modifier.fillMaxWidth(),
                style = PesaButtonStyle.TextMuted,
            )
        }
    }
}

private val UserBubbleShape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomEnd = 4.dp, bottomStart = 14.dp)

/** Decorative sample of an answer. Hidden from accessibility. */
@Composable
private fun PreviewCard(modifier: Modifier = Modifier) {
    val colors = PesaLensTheme.colors
    val type = PesaLensTheme.type
    val figure = type.meta.copy(fontFeatureSettings = "tnum")
    Column(
        modifier
            .clearAndSetSemantics {}
            .clip(PesaLensTheme.shapes.card)
            .background(colors.surface)
            .border(1.dp, colors.border, PesaLensTheme.shapes.card)
            .padding(PesaSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            stringResource(R.string.welcome_preview_question),
            modifier = Modifier
                .align(Alignment.End)
                .background(colors.tint, UserBubbleShape)
                .padding(horizontal = PesaSpacing.md, vertical = PesaSpacing.sm),
            style = type.meta,
            color = colors.text,
        )
        Text(stringResource(R.string.welcome_preview_period), style = type.kicker.copy(letterSpacing = TextUnit.Unspecified), color = colors.textMuted)
        Row(
            Modifier.fillMaxWidth().height(BarHeight).clip(PesaLensTheme.shapes.track),
            horizontalArrangement = Arrangement.spacedBy(PesaSpacing.xxs),
        ) {
            BarWeights.forEachIndexed { tone, weight ->
                Box(
                    Modifier
                        .weight(weight)
                        .fillMaxSize()
                        .patternFill(ChartPattern.forTone(tone), colors.chart[tone], colors.patternInk),
                )
            }
        }
        AmountRow(stringResource(R.string.welcome_preview_rent), stringResource(R.string.welcome_preview_rent_amount), figure)
        AmountRow(stringResource(R.string.welcome_preview_food), stringResource(R.string.welcome_preview_food_amount), figure)
    }
}

@Composable
private fun AmountRow(label: String, amount: String, style: TextStyle) {
    val colors = PesaLensTheme.colors
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = style, color = colors.text)
        Text(amount, style = style, color = colors.text)
    }
}
