package lutukai.pesalens.core.ui.mvi

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

private data class CounterState(val count: Int = 0) : UiState

private sealed interface CounterIntent : UiIntent {
    data object Increment : CounterIntent
    data object Announce : CounterIntent
}

private data class CounterEffect(val value: Int) : UiEffect

private class CounterViewModel : MviViewModel<CounterState, CounterIntent, CounterEffect>(CounterState()) {
    override fun handleIntent(intent: CounterIntent) {
        when (intent) {
            CounterIntent.Increment -> reduce { copy(count = count + 1) }
            CounterIntent.Announce -> emit(CounterEffect(currentState.count))
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MviViewModelTest {
    @Before fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun stateStartsAtInitialValue() {
        assertEquals(CounterState(0), CounterViewModel().state.value)
    }

    @Test fun reduceUpdatesState() {
        val vm = CounterViewModel()
        vm.onIntent(CounterIntent.Increment)
        vm.onIntent(CounterIntent.Increment)
        assertEquals(2, vm.state.value.count)
    }

    @Test fun effectsArriveInOrder() = runTest {
        val vm = CounterViewModel()
        vm.effects.test {
            vm.onIntent(CounterIntent.Announce)
            vm.onIntent(CounterIntent.Increment)
            vm.onIntent(CounterIntent.Announce)
            assertEquals(CounterEffect(0), awaitItem())
            assertEquals(CounterEffect(1), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test fun effectsEmittedBeforeCollectionAreBuffered() = runTest {
        val vm = CounterViewModel()
        vm.onIntent(CounterIntent.Announce)
        vm.onIntent(CounterIntent.Increment)
        vm.onIntent(CounterIntent.Announce)
        vm.effects.test {
            assertEquals(CounterEffect(0), awaitItem())
            assertEquals(CounterEffect(1), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
