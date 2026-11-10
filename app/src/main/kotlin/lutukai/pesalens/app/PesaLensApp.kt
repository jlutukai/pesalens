package lutukai.pesalens.app

import android.content.res.Resources
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirectiveWithTwoPanesOnMediumWidth
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import lutukai.pesalens.core.designsystem.component.PesaSnackbarHost
import lutukai.pesalens.core.designsystem.theme.PesaColors
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.domain.repository.SettingsRepository
import lutukai.pesalens.core.model.ThemePreference
import lutukai.pesalens.core.navigation.BackStackNavigator
import lutukai.pesalens.core.navigation.LocalNavigator
import lutukai.pesalens.core.navigation.PesaDestination
import lutukai.pesalens.core.navigation.rememberAdaptiveOverlaySceneStrategy
import lutukai.pesalens.core.ui.message.UserMessageQueue
import lutukai.pesalens.core.ui.text.UiText
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI

/** Prototype: the snackbar sits 96dp up, clear of the assistant's composer. */
private val SnackbarBottomOffset = 96.dp

// Navigation bar scrims at the platform defaults' opacities, in PesaLens colours. Only API 26-28 three-button
// navigation shows them (API 24-25 always the dark one); later releases keep the bar transparent and protect it.
private val LightNavigationScrim = PesaColors.Light.background.copy(alpha = 0.9f).toArgb()
private val DarkNavigationScrim = PesaColors.Dark.background.copy(alpha = 0.5f).toArgb()

/** The app shell: theme and system bars, the one [NavDisplay], the app lock gate and the app-wide snackbar host. */
@Composable
fun PesaLensApp() {
    val settings by koinInject<SettingsRepository>().settings.collectAsStateWithLifecycle(initialValue = null)
    val darkTheme = when (settings?.theme) {
        ThemePreference.Dark -> true
        ThemePreference.Light -> false
        ThemePreference.System, null -> isSystemInDarkTheme()
    }
    SystemBarsFor(darkTheme)
    PesaLensTheme(darkTheme = darkTheme) {
        Box(Modifier.fillMaxSize().background(PesaLensTheme.colors.background)) {
            // Just the background until settings arrive: they decide where the back stack starts.
            settings?.let { AppNavigation(startDestination(it)) }
            UserMessageHost(
                queue = koinInject(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(bottom = SnackbarBottomOffset),
            )
        }
    }
}

/**
 * The back stack, its [LocalNavigator] and the [NavDisplay]. [startDestination] only seeds a new back stack: the stack
 * is saved and restored from then on, so later settings changes never replace it.
 */
@OptIn(KoinExperimentalAPI::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun AppNavigation(startDestination: PesaDestination) {
    val backStack = rememberNavBackStack(startDestination)
    val navigator = remember(backStack) { BackStackNavigator(backStack) }
    CompositionLocalProvider(LocalNavigator provides navigator) {
        NavDisplay(
            backStack = backStack,
            onBack = navigator::back,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            sceneStrategies = listOf(
                rememberAdaptiveOverlaySceneStrategy<NavKey>(),
                // Medium widths (600-839dp, e.g. an unfolded foldable) split too; the default directive keeps one pane.
                rememberListDetailSceneStrategy<NavKey>(
                    directive = calculatePaneScaffoldDirectiveWithTwoPanesOnMediumWidth(currentWindowAdaptiveInfoV2()),
                ),
            ),
            entryProvider = koinEntryProvider<NavKey>(),
        )
    }
    AppLockGate(navigator)
}

/** The bar icons follow the app's theme, which can differ from the system's dark mode setting. */
@Composable
private fun SystemBarsFor(darkTheme: Boolean) {
    val activity = LocalActivity.current as? ComponentActivity ?: return
    DisposableEffect(activity, darkTheme) {
        activity.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
            navigationBarStyle = SystemBarStyle.auto(LightNavigationScrim, DarkNavigationScrim) { darkTheme },
        )
        onDispose {}
    }
}

/** Shows each message posted to [queue] as a snackbar, one at a time and in order. */
@Composable
private fun UserMessageHost(queue: UserMessageQueue, modifier: Modifier = Modifier) {
    val hostState = remember { SnackbarHostState() }
    // Read per message, so a configuration change doesn't restart the collector and cut off the one showing.
    val resources by rememberUpdatedState(LocalResources.current)
    LaunchedEffect(queue) {
        queue.messages.collect { message -> hostState.showSnackbar(message.resolve(resources)) }
    }
    PesaSnackbarHost(hostState, modifier)
}

/** [UiText.asString][lutukai.pesalens.core.ui.text.asString] for use outside composition. */
private fun UiText.resolve(resources: Resources): String = when (this) {
    is UiText.Raw -> value
    is UiText.Res -> resources.getString(id, *args.resolveEach(resources))
    is UiText.Plural -> resources.getQuantityString(id, count, *args.resolveEach(resources))
}

private fun List<Any>.resolveEach(resources: Resources): Array<Any> =
    Array(size) { i -> (this[i] as? UiText)?.resolve(resources) ?: this[i] }
