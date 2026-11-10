package lutukai.pesalens.feature.assistant.impl.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.designsystem.theme.PesaSpacing
import lutukai.pesalens.feature.assistant.impl.R

/** Fills the detail pane of the list-detail layout while nothing is open beside the conversation. */
@Composable
fun DetailPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier.fillMaxSize().background(PesaLensTheme.colors.surfaceAlt).padding(PesaSpacing.xxl),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            stringResource(R.string.assistant_detail_placeholder),
            style = PesaLensTheme.type.body,
            color = PesaLensTheme.colors.textMuted,
            textAlign = TextAlign.Center,
        )
    }
}
