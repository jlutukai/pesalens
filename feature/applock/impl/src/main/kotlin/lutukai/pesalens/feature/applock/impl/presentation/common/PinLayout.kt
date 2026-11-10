package lutukai.pesalens.feature.applock.impl.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import lutukai.pesalens.core.designsystem.adaptive.PesaWidthClass
import lutukai.pesalens.core.designsystem.adaptive.rememberPesaWindowInfo
import lutukai.pesalens.core.designsystem.component.PesaPinDots
import lutukai.pesalens.core.designsystem.component.PesaPinPad
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.designsystem.theme.PesaSpacing
import lutukai.pesalens.feature.applock.impl.R

/** Test tag of the four PIN dots, so tests can check how many are filled. */
const val PinDotsTag = "pin-dots"

private val ColumnMaxWidth = 480.dp
private val ErrorLineHeight = 20.dp

/**
 * Medium and wider windows get a 480dp centred column instead of the full width. Compact-height windows keep the
 * full width, since their header and keypad sit side by side.
 */
@Composable
fun CenteredColumn(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val window = rememberPesaWindowInfo()
    val wide = window.widthClass != PesaWidthClass.Compact && !window.isCompactHeight
    Box(modifier, contentAlignment = Alignment.TopCenter) {
        Box(if (wide) Modifier.widthIn(max = ColumnMaxWidth) else Modifier.fillMaxWidth()) { content() }
    }
}

/**
 * The PIN screen body shared by setup and the locked screen: a [header], the four dots, a polite live-region error
 * line and the keypad. On compact-height windows the header block and the keypad sit side by side.
 */
@Composable
fun PinLayout(
    digits: Int,
    error: String?,
    onDigit: (Char) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    keypadEnabled: Boolean = true,
    header: @Composable ColumnScope.() -> Unit,
) {
    val compactHeight = rememberPesaWindowInfo().isCompactHeight
    val keypad = @Composable {
        PesaPinPad(
            onDigit = onDigit,
            onDelete = onDelete,
            deleteContentDescription = stringResource(R.string.applock_delete_digit),
            enabled = keypadEnabled,
        )
    }
    if (compactHeight) {
        Row(modifier, horizontalArrangement = Arrangement.spacedBy(PesaSpacing.xxxl), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                header()
                DotsAndError(digits, error)
            }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { keypad() }
        }
    } else {
        Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            header()
            DotsAndError(digits, error)
            Spacer(Modifier.weight(1f).heightIn(min = PesaSpacing.lg))
            keypad()
        }
    }
}

@Composable
private fun DotsAndError(digits: Int, error: String?) {
    PesaPinDots(filled = digits, isError = error != null, modifier = Modifier.padding(top = 28.dp, bottom = 10.dp).testTag(PinDotsTag))
    Text(
        text = error.orEmpty(),
        modifier = Modifier.heightIn(min = ErrorLineHeight).semantics { liveRegion = LiveRegionMode.Polite },
        style = PesaLensTheme.type.meta,
        color = PesaLensTheme.colors.warnText,
        textAlign = TextAlign.Center,
    )
}

/** Title and subtitle under a header tile: 22/500 with an 18dp top margin, 14 muted with 4dp. */
@Composable
fun ColumnScope.TitleBlock(title: String, subtitle: String) {
    Text(
        title,
        modifier = Modifier.padding(top = 18.dp),
        style = PesaLensTheme.type.titleLarge,
        color = PesaLensTheme.colors.text,
    )
    Text(
        subtitle,
        modifier = Modifier.padding(top = PesaSpacing.xs),
        style = PesaLensTheme.type.bodySmall,
        color = PesaLensTheme.colors.textMuted,
        textAlign = TextAlign.Center,
    )
}
