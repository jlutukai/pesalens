package lutukai.pesalens.feature.onboarding.impl.presentation.consent

import android.graphics.Typeface
import android.text.Spanned
import android.text.style.StyleSpan
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import lutukai.pesalens.core.designsystem.component.PesaButton
import lutukai.pesalens.core.designsystem.component.PesaButtonStyle
import lutukai.pesalens.core.designsystem.icon.PesaIcons
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.designsystem.theme.PesaSpacing
import lutukai.pesalens.core.navigation.LocalNavigator
import lutukai.pesalens.core.ui.mvi.ObserveAsEvents
import lutukai.pesalens.feature.applock.api.AppLockSetup
import lutukai.pesalens.feature.applock.api.LockSetupOrigin
import lutukai.pesalens.feature.onboarding.api.ConsentOrigin
import lutukai.pesalens.feature.onboarding.impl.R
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Body of the overlay entry. The overlay scene draws the sheet or dialog chrome and its padding. */
@Composable
fun CloudConsentSheet(origin: ConsentOrigin) {
    val viewModel: CloudConsentViewModel = koinViewModel { parametersOf(origin) }
    val navigator = LocalNavigator.current
    ObserveAsEvents(viewModel.effects) { effect ->
        when (effect) {
            CloudConsentEffect.Close -> navigator.back()
            CloudConsentEffect.ContinueOnboarding -> navigator.replaceAll(AppLockSetup(LockSetupOrigin.Onboarding))
        }
    }
    CloudConsentContent(onIntent = viewModel::onIntent)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CloudConsentContent(onIntent: (CloudConsentIntent) -> Unit, modifier: Modifier = Modifier) {
    val colors = PesaLensTheme.colors
    val type = PesaLensTheme.type
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Icon(PesaIcons.Cloud, contentDescription = null, modifier = Modifier.size(26.dp), tint = colors.accentText)
        Column(verticalArrangement = Arrangement.spacedBy(PesaSpacing.xs)) {
            Text(stringResource(R.string.consent_title), style = type.titleMedium, color = colors.text)
            Text(stringResource(R.string.consent_body), style = type.bodySmall, color = colors.textMuted)
        }
        Column(
            Modifier
                .fillMaxWidth()
                .background(colors.surfaceAlt, InfoShape)
                .border(1.dp, colors.border, InfoShape)
                // 14dp padding inside the 1dp border, as in CSS.
                .padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            InfoRow(PesaIcons.ArrowUpRight, R.string.consent_sent)
            InfoRow(PesaIcons.LockSimple, R.string.consent_not_sent)
            InfoRow(PesaIcons.WifiHigh, R.string.consent_needs_internet)
        }
        // At large font sizes a button that no longer fits moves to its own line rather than wrapping its label.
        FlowRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PesaSpacing.sm, Alignment.End),
            verticalArrangement = Arrangement.spacedBy(PesaSpacing.sm),
        ) {
            PesaButton(
                text = stringResource(R.string.consent_not_now),
                onClick = { onIntent(CloudConsentIntent.NotNow) },
                style = PesaButtonStyle.Text,
            )
            PesaButton(
                text = stringResource(R.string.consent_agree),
                onClick = { onIntent(CloudConsentIntent.Agree) },
                style = PesaButtonStyle.Tonal,
            )
        }
    }
}

private val InfoShape = RoundedCornerShape(16.dp)

@Composable
private fun InfoRow(icon: ImageVector, @StringRes text: Int) {
    val colors = PesaLensTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = colors.accentText)
        Text(styledString(text), style = PesaLensTheme.type.bodySmall, color = colors.text)
    }
}

/** A string resource with `<b>` runs rendered at the theme's medium weight (the prototype's 500), not bold. */
@Composable
private fun styledString(@StringRes id: Int): AnnotatedString {
    val resources = LocalResources.current
    return remember(id, resources) {
        val text = resources.getText(id)
        buildAnnotatedString {
            append(text.toString())
            (text as? Spanned)?.getSpans(0, text.length, StyleSpan::class.java)?.forEach { span ->
                if (span.style == Typeface.BOLD) {
                    addStyle(
                        SpanStyle(fontWeight = FontWeight.Medium),
                        text.getSpanStart(span),
                        text.getSpanEnd(span),
                    )
                }
            }
        }
    }
}
