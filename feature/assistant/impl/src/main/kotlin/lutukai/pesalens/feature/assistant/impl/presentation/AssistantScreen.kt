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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
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
    val onIntent = remember(viewModel) { viewModel::onIntent }
    AssistantContent(state = state, onIntent = onIntent)
}

@Composable
fun AssistantContent(state: AssistantUiState, onIntent: (AssistantIntent) -> Unit, modifier: Modifier = Modifier) {
    val colors = PesaLensTheme.colors
    val listState = rememberLazyListState()
    val items = state.items
    val busy = state.busy
    val currentMonth = state.currentMonth
    val modelReady = state.modelReady
    val mode = state.mode
    val lastId = items.lastOrNull()?.id

    // The last item id already scrolled to. Ids are never reused, so a reply that replaces the Thinking item (same
    // size, new id) still scrolls; saved so a rotation doesn't jump.
    var handledId by rememberSaveable { mutableLongStateOf(-1L) }
    LaunchedEffect(lastId, items.size) {
        if (lastId != null && lastId != handledId) listState.scrollToEnd(lastIndex = items.size + 1)
        if (lastId != null) handledId = lastId
    }

    // Keep the end in view when the keyboard opens, if the list was at its end before it did.
    val imeOpen = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val imeNow by rememberUpdatedState(imeOpen)
    var atEnd by remember { mutableStateOf(true) }
    LaunchedEffect(listState) {
        snapshotFlow { !listState.canScrollForward }.collect { if (!imeNow) atEnd = it }
    }
    LaunchedEffect(imeOpen) {
        if (imeOpen && atEnd && items.isNotEmpty()) listState.scrollToEnd(lastIndex = items.size + 1)
    }

    // The last item that isn't the user's: the only one that offers follow-up chips (prototype lastBot).
    val lastBotId = items.lastOrNull { it !is ChatItem.User }?.id

    Column(modifier.fillMaxSize().background(colors.background).windowInsetsPadding(WindowInsets.safeDrawing)) {
        TopBar(mode, onSettings = { onIntent(AssistantIntent.OpenSettings) })
        LazyColumn(
            Modifier.weight(1f).fillMaxWidth(),
            state = listState,
            contentPadding = PaddingValues(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item(key = "banner") { ColumnSlot { ModeBanner(mode) } }
            item(key = "greeting") {
                ColumnSlot {
                    Column(verticalArrangement = Arrangement.spacedBy(PesaSpacing.sm)) {
                        AssistantBubble(stringResource(R.string.assistant_greeting))
                        if (items.isEmpty()) Chips(IntroSuggestions, onIntent)
                    }
                }
            }
            items(items, key = { it.id }) { item ->
                ColumnSlot {
                    Column(verticalArrangement = Arrangement.spacedBy(PesaSpacing.sm)) {
                        ChatItemView(item, busy, currentMonth, modelReady, onIntent)
                        item.captionMode()?.let { MessageCaption(it) }
                        if (item.id == lastBotId) Chips(suggestionsAfter(item), onIntent)
                    }
                }
            }
        }
        Composer(state.draft, busy, onIntent)
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
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TopBar(mode: AssistantMode, onSettings: () -> Unit) {
    val colors = PesaLensTheme.colors
    val border = colors.border
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .drawBehind {
                val w = 1.dp.toPx()
                drawLine(border, Offset(0f, size.height - w / 2), Offset(size.width, size.height - w / 2), w)
            }
            .padding(start = PesaSpacing.xl, end = 6.dp, top = PesaSpacing.sm, bottom = PesaSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PesaSpacing.sm),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            // 18sp with the prototype's 1.1 line height, so the pill row sits close under the title.
            Text(stringResource(R.string.assistant_title), style = PesaLensTheme.type.title.copy(lineHeight = 19.8.sp), color = colors.text)
            FlowRow(
                verticalArrangement = Arrangement.spacedBy(PesaSpacing.xs),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                ModePill(mode)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(PesaSpacing.xs)) {
                    Icon(PesaIcons.Database, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(12.dp))
                    Text(
                        stringResource(R.string.assistant_sample_data),
                        style = PesaLensTheme.type.kicker.copy(letterSpacing = TextUnit.Unspecified),
                        color = colors.textMuted,
                    )
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
private fun Composer(draft: String, busy: Boolean, onIntent: (AssistantIntent) -> Unit) {
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
            value = draft,
            onValueChange = { onIntent(AssistantIntent.DraftChanged(it)) },
            onSend = { onIntent(AssistantIntent.Send) },
            sendEnabled = draft.isNotBlank() && !busy,
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
