package lutukai.pesalens.app

import android.graphics.drawable.ColorDrawable
import android.view.ContextThemeWrapper
import androidx.compose.ui.graphics.toArgb
import lutukai.pesalens.core.designsystem.theme.PesaColors
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

/** The cold-start window, drawn before Compose, matches the app's background in the system's light or dark mode. */
@RunWith(RobolectricTestRunner::class)
class WindowBackgroundTest {

    /** The test application starts Koin; the next test's application starts it again. */
    @After
    fun stopKoinAfter() = stopKoin()

    @Test
    fun colours_matchTheNocturneBackgroundTokens() {
        val app = RuntimeEnvironment.getApplication()
        assertEquals(PesaColors.Light.background.toArgb(), app.getColor(R.color.window_background_light))
        assertEquals(PesaColors.Dark.background.toArgb(), app.getColor(R.color.window_background_dark))
    }

    @Test
    fun lightSystemTheme_hasTheLightWindow() {
        assertEquals(PesaColors.Light.background.toArgb(), windowBackground())
    }

    @Test
    @Config(qualifiers = "night")
    fun darkSystemTheme_hasTheDarkWindow() {
        assertEquals(PesaColors.Dark.background.toArgb(), windowBackground())
    }

    private fun windowBackground(): Int {
        val themed = ContextThemeWrapper(RuntimeEnvironment.getApplication(), R.style.Theme_Pesalens)
        val attrs = themed.obtainStyledAttributes(intArrayOf(android.R.attr.windowBackground))
        try {
            return (attrs.getDrawable(0) as ColorDrawable).color
        } finally {
            attrs.recycle()
        }
    }
}
