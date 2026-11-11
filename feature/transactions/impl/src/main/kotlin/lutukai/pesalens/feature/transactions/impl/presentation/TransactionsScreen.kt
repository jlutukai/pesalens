package lutukai.pesalens.feature.transactions.impl.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.YearMonth
import lutukai.pesalens.core.designsystem.adaptive.PesaWidthClass
import lutukai.pesalens.core.designsystem.adaptive.rememberPesaWindowInfo
import lutukai.pesalens.core.designsystem.component.CalloutTone
import lutukai.pesalens.core.designsystem.component.PesaButton
import lutukai.pesalens.core.designsystem.component.PesaButtonStyle
import lutukai.pesalens.core.designsystem.component.PesaCallout
import lutukai.pesalens.core.designsystem.component.PesaCard
import lutukai.pesalens.core.designsystem.component.PesaPill
import lutukai.pesalens.core.designsystem.component.PesaTopBar
import lutukai.pesalens.core.designsystem.component.PillTone
import lutukai.pesalens.core.designsystem.icon.PesaIcons
import lutukai.pesalens.core.designsystem.pattern.ChartPattern
import lutukai.pesalens.core.designsystem.pattern.patternFill
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.designsystem.theme.PesaSpacing
import lutukai.pesalens.core.model.Money
import lutukai.pesalens.core.navigation.LocalNavigator
import lutukai.pesalens.core.ui.catalog.StatusNotice
import lutukai.pesalens.core.ui.catalog.TransactionRow
import lutukai.pesalens.core.ui.catalog.model.NoticeKind
import lutukai.pesalens.core.ui.format.formatKes
import lutukai.pesalens.core.ui.format.formatLong
import lutukai.pesalens.core.ui.mvi.ObserveAsEvents
import lutukai.pesalens.core.ui.text.asString
import lutukai.pesalens.feature.transactions.api.CategoryTransactions
import lutukai.pesalens.feature.transactions.impl.R
import org.koin.compose.viewmodel.koinViewModel
import lutukai.pesalens.core.ui.R as CoreUiR

/** Content never grows wider than this; on wide panes it is centred. */
private val ContentMaxWidth = 640.dp
private val SwatchSize = 44.dp

@Composable
fun TransactionsScreen(destination: CategoryTransactions, viewModel: TransactionsViewModel = koinViewModel()) {
    val navigator = LocalNavigator.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel, destination) {
        viewModel.onIntent(TransactionsIntent.Load(destination.categoryId, YearMonth(destination.year, destination.month)))
    }
    ObserveAsEvents(viewModel.effects) { effect ->
        when (effect) {
            TransactionsEffect.Close -> navigator.back()
        }
    }
    val onIntent = remember(viewModel) { viewModel::onIntent }
    TransactionsContent(state = state, onIntent = onIntent)
}

@Composable
fun TransactionsContent(state: TransactionsUiState, onIntent: (TransactionsIntent) -> Unit, modifier: Modifier = Modifier) {
    val colors = PesaLensTheme.colors
    val showBackButton = rememberPesaWindowInfo().widthClass == PesaWidthClass.Compact
    Column(
        modifier
            .fillMaxSize()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        when (state) {
            TransactionsUiState.Loading -> {
                TopBar(title = "", subtitle = null, onBack = { onIntent(TransactionsIntent.Back) }, tagged = false)
            }
            is TransactionsUiState.Content -> {
                TopBar(
                    title = state.label,
                    subtitle = stringResource(R.string.transactions_subtitle, state.month.formatLong(), state.rangeLabel),
                    onBack = { onIntent(TransactionsIntent.Back) },
                    tagged = true,
                )
                Body { ContentBody(state, showBackButton, onIntent) }
            }
            is TransactionsUiState.Empty -> {
                TopBar(title = state.label, subtitle = state.month.formatLong(), onBack = { onIntent(TransactionsIntent.Back) }, tagged = true)
                Body {
                    StatusNotice(
                        kind = NoticeKind.NoData,
                        title = stringResource(CoreUiR.string.core_ui_notice_no_data_title, state.month.formatLong()),
                        body = stringResource(CoreUiR.string.core_ui_notice_no_data_body),
                        actions = persistentListOf(),
                        onAction = {},
                    )
                }
            }
            is TransactionsUiState.Error -> {
                TopBar(title = "", subtitle = null, onBack = { onIntent(TransactionsIntent.Back) }, tagged = false)
                Body {
                    PesaCallout(
                        text = stringResource(R.string.transactions_error),
                        icon = PesaIcons.Warning,
                        tone = CalloutTone.Warn,
                        liveRegion = true,
                    )
                    PesaButton(
                        text = stringResource(R.string.transactions_retry),
                        onClick = { onIntent(TransactionsIntent.Retry) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun TopBar(title: String, subtitle: String?, onBack: () -> Unit, tagged: Boolean) {
    PesaTopBar(
        title = title,
        subtitle = subtitle,
        onBack = onBack,
        backContentDescription = stringResource(R.string.transactions_back),
        titleStyle = PesaLensTheme.type.titleSmall,
        actions = {
            if (tagged) PesaPill(stringResource(R.string.transactions_sample_tag), PillTone.Dashed, icon = PesaIcons.Database, height = 24.dp)
        },
    )
}

/** The scrolling, width-capped body under the top bar. */
@Composable
private fun Body(content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(PesaSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            Modifier.widthIn(max = ContentMaxWidth).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(PesaSpacing.lg),
        ) { content() }
    }
}

@Composable
private fun ContentBody(state: TransactionsUiState.Content, showBackButton: Boolean, onIntent: (TransactionsIntent) -> Unit) {
    val colors = PesaLensTheme.colors
    val type = PesaLensTheme.type
    TotalCard(state)
    Column {
        Text(
            stringResource(R.string.transactions_list_title),
            modifier = Modifier.padding(start = 2.dp, end = 2.dp, bottom = PesaSpacing.sm),
            style = type.meta.copy(fontWeight = FontWeight.Medium),
            color = colors.textMuted,
        )
        state.transactions.forEach { TransactionRow(it) }
        val rule = colors.text
        Row(
            Modifier
                .fillMaxWidth()
                .drawBehind {
                    val w = 1.dp.toPx()
                    drawLine(rule, Offset(0f, w / 2), Offset(size.width, w / 2), w)
                }
                .heightIn(min = PesaSpacing.minTouchTarget),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.transactions_total), style = type.bodyStrong, color = colors.text)
            Text(state.total.formatKes(), style = type.bodyStrong.copy(fontFeatureSettings = "tnum"), color = colors.text)
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(PesaSpacing.sm)) {
        Icon(PesaIcons.Info, contentDescription = null, tint = colors.textMuted, modifier = Modifier.padding(top = 1.dp).size(16.dp))
        Text(stringResource(R.string.transactions_footnote), style = type.meta, color = colors.textMuted)
    }
    if (showBackButton) {
        PesaButton(
            text = stringResource(R.string.transactions_back),
            onClick = { onIntent(TransactionsIntent.Back) },
            modifier = Modifier.fillMaxWidth(),
            style = PesaButtonStyle.Outlined,
        )
    }
}

@Composable
private fun TotalCard(state: TransactionsUiState.Content) {
    val colors = PesaLensTheme.colors
    val type = PesaLensTheme.type
    PesaCard(Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            val shape = RoundedCornerShape(12.dp)
            Box(
                Modifier
                    .size(SwatchSize)
                    .clip(shape)
                    .patternFill(ChartPattern.forTone(state.tone), colors.chart[state.tone], colors.patternInk)
                    .border(1.dp, colors.border, shape),
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    stringResource(R.string.transactions_category_total).uppercase(),
                    style = type.kicker,
                    color = colors.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                // 28sp at the prototype's inherited 1.5 line height (this figure sets no line height of its own).
                Text(state.total.formatKes(), style = type.headline.copy(fontFeatureSettings = "tnum", lineHeight = 42.sp), color = colors.text)
                Text(state.meta.asString(), style = type.meta, color = colors.textMuted)
            }
        }
    }
}
