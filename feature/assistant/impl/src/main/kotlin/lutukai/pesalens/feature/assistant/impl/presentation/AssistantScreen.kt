package lutukai.pesalens.feature.assistant.impl.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.number
import lutukai.pesalens.core.designsystem.component.PesaComposer
import lutukai.pesalens.core.designsystem.component.PesaSuggestionChip
import lutukai.pesalens.core.designsystem.icon.PesaIcons
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.designsystem.theme.PesaSpacing
import lutukai.pesalens.core.navigation.LocalNavigator
import lutukai.pesalens.core.ui.catalog.AssistantBubble
import lutukai.pesalens.core.ui.catalog.BudgetReviewDialog
import lutukai.pesalens.core.ui.catalog.MessageCaption
import lutukai.pesalens.core.ui.catalog.ModeBanner
import lutukai.pesalens.core.ui.catalog.ModePill
import lutukai.pesalens.core.ui.catalog.SuggestionRow
import lutukai.pesalens.core.model.AssistantMode
import kotlinx.collections.immutable.ImmutableList
import lutukai.pesalens.core.ui.mvi.ObserveAsEvents
import lutukai.pesalens.feature.assistant.impl.R
import lutukai.pesalens.feature.modelsetup.api.ModelSetup
import lutukai.pesalens.feature.onboarding.api.CloudConsent
import lutukai.pesalens.feature.onboarding.api.ConsentOrigin
import lutukai.pesalens.feature.settings.api.Settings
import lutukai.pesalens.feature.transactions.api.CategoryTransactions
import org.koin.compose.viewmodel.koinViewModel

/** The message column never grows wider than this; on wider panes it is centred. */
private val ColumnMaxWidth = 720.dp

@Composable
fun AssistantScreen(viewModel: AssistantViewModel = koinViewModel()) {
    val navigator = LocalNavigator.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.onIntent(AssistantIntent.Load) }
    ObserveAsEvents(viewModel.effects) { effect ->
        when (effect) {
            is AssistantEffect.OpenCategory ->
                navigator.navigate(CategoryTransactions(effect.categoryId, effect.month.year, effect.month.month.number))
            AssistantEffect.OpenSettings -> navigator.navigate(Settings)
            is AssistantEffect.OpenModelSetup -> navigator.navigate(ModelSetup(effect.origin))
            AssistantEffect.OpenCloudConsent -> navigator.navigate(CloudConsent(ConsentOrigin.Assistant))
        }
    }
    AssistantContent(state = state, onIntent = viewModel::onIntent)
}

@Composable
fun AssistantContent(state: AssistantUiState, onIntent: (AssistantIntent) -> Unit, modifier: Modifier = Modifier) {
    val colors = PesaLensTheme.colors
    val listState = rememberLazyListState()
    var knownSize by remember { mutableIntStateOf(0) }
    LaunchedEffect(state.items.size) {
        // On entry a conversation opens at its end; afterwards only a growing list scrolls.
        if (state.items.size > knownSize) listState.scrollToEnd(lastIndex = state.items.size + 1)
        knownSize = state.items.size
    }
    // The last item that isn't the user's: the only one that offers follow-up chips (prototype lastBot).
    val lastBotId = state.items.lastOrNull { it !is ChatItem.User }?.id

    Column(modifier.fillMaxSize().background(colors.background).windowInsetsPadding(WindowInsets.safeDrawing)) {
        TopBar(state.mode, onSettings = { onIntent(AssistantIntent.OpenSettings) })
        LazyColumn(
            Modifier.weight(1f).fillMaxWidth(),
            state = listState,
            contentPadding = PaddingValues(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item(key = "banner") { ColumnSlot { ModeBanner(state.mode) } }
            item(key = "greeting") {
                ColumnSlot {
                    Column(verticalArrangement = Arrangement.spacedBy(PesaSpacing.sm)) {
                        AssistantBubble(stringResource(R.string.assistant_greeting))
                        if (state.items.isEmpty()) Chips(IntroSuggestions, onIntent)
                    }
                }
            }
            items(state.items, key = { it.id }) { item ->
                ColumnSlot {
                    Column(verticalArrangement = Arrangement.spacedBy(PesaSpacing.sm)) {
                        ChatItemView(item, state, onIntent)
                        item.captionMode()?.let { MessageCaption(it) }
                        if (item.id == lastBotId) Chips(suggestionsAfter(item), onIntent)
                    }
                }
            }
        }
        Composer(state, onIntent)
    }

    when (val dialog = state.dialog) {
        AssistantDialog.None -> Unit
        is AssistantDialog.ReviewBudget -> {
            val budget = state.items.firstOrNull { it.id == dialog.itemId } as? ChatItem.Budget
            if (budget != null) {
                BudgetReviewDialog(
                    ui = budget.ui,
                    onConfirm = { onIntent(AssistantIntent.ConfirmSave) },
                    onDismiss = { onIntent(AssistantIntent.DismissDialog) },
                )
            }
        }
    }
}

@Composable
private fun ColumnSlot(content: @Composable () -> Unit) {
    Box(Modifier.widthIn(max = ColumnMaxWidth).fillMaxWidth()) { content() }
}

/** Bot items carry a caption naming the AI mode that produced them; the user's bubble and the loading bubble don't. */
private fun ChatItem.captionMode(): AssistantMode? = when (this) {
    is ChatItem.User, is ChatItem.Thinking -> null
    is ChatItem.AssistantText -> mode
    is ChatItem.SpendingResult -> mode
    is ChatItem.PriceQuestion -> mode
    is ChatItem.AffordabilityDetails -> mode
    is ChatItem.AffordabilityResult -> mode
    is ChatItem.Budget -> mode
    is ChatItem.BudgetSaved -> mode
    is ChatItem.Notice -> mode
}

@Composable
private fun Chips(suggestions: ImmutableList<Suggestion>, onIntent: (AssistantIntent) -> Unit) {
    if (suggestions.isEmpty()) return
    val labels = suggestions.map { it.label() }.toImmutableList()
    SuggestionRow(labels) { index -> onIntent(AssistantIntent.SuggestionTapped(suggestions[index])) }
}

@Composable
private fun Suggestion.label(): String = when (this) {
    is Suggestion.Ask -> stringResource(prompt.label)
    is Suggestion.OpenCategory -> stringResource(R.string.assistant_suggestion_show_category, label.replaceFirstChar { it.lowercase() })
    Suggestion.TrySaving15k -> stringResource(R.string.assistant_suggestion_try_saving_15k)
}

/** 64dp bar: title over the mode pill and "Sample data", 48dp settings button; bottom 1dp border. */
@Composable
private fun TopBar(mode: AssistantMode, onSettings: () -> Unit) {
    val colors = PesaLensTheme.colors
    val border = colors.border
    Row(
        Modifier
            .fillMaxWidth()
            .height(64.dp)
            .drawBehind {
                val w = 1.dp.toPx()
                drawLine(border, Offset(0f, size.height - w / 2), Offset(size.width, size.height - w / 2), w)
            }
            .padding(start = PesaSpacing.xl, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PesaSpacing.sm),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(stringResource(R.string.assistant_title), style = PesaLensTheme.type.titleSmall, color = colors.text)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ModePill(mode)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(PesaSpacing.xs)) {
                    Icon(PesaIcons.Database, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(14.dp))
                    Text(stringResource(R.string.assistant_sample_data), style = PesaLensTheme.type.caption, color = colors.textMuted)
                }
            }
        }
        val settings = stringResource(R.string.assistant_settings)
        Box(
            Modifier
                .size(PesaSpacing.minTouchTarget)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClick = onSettings)
                .semantics { contentDescription = settings },
            contentAlignment = Alignment.Center,
        ) {
            Icon(PesaIcons.GearSix, contentDescription = null, tint = colors.text, modifier = Modifier.size(22.dp))
        }
    }
}

/** Pinned bottom bar: top border, background colour; the field matches the message column's width. */
@Composable
private fun Composer(state: AssistantUiState, onIntent: (AssistantIntent) -> Unit) {
    val colors = PesaLensTheme.colors
    val border = colors.border
    Box(
        Modifier
            .fillMaxWidth()
            .background(colors.background)
            .drawBehind {
                val w = 1.dp.toPx()
                drawLine(border, Offset(0f, w / 2), Offset(size.width, w / 2), w)
            }
            .padding(start = 16.dp, top = 8.dp, end = 12.dp, bottom = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        val placeholder = stringResource(R.string.assistant_composer_placeholder)
        PesaComposer(
            value = state.draft,
            onValueChange = { onIntent(AssistantIntent.DraftChanged(it)) },
            onSend = { onIntent(AssistantIntent.Send) },
            sendEnabled = state.draft.isNotBlank() && !state.busy,
            placeholder = placeholder,
            sendContentDescription = stringResource(R.string.assistant_send),
            modifier = Modifier.widthIn(max = ColumnMaxWidth),
        )
    }
}

/** Brings the end of the last item into view; an item taller than the viewport would otherwise hide its chips. */
private suspend fun LazyListState.scrollToEnd(lastIndex: Int) {
    scrollToItem(lastIndex)
    scrollBy(Float.MAX_VALUE)
}
