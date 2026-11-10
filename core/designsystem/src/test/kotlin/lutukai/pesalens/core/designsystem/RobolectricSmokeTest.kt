package lutukai.pesalens.core.designsystem

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.junit.runner.RunWith

/** Proves the build-logic test setup boots Robolectric on the project's SDK. Later tasks may replace it. */
@RunWith(AndroidJUnit4::class)
class RobolectricSmokeTest {

    @Test
    fun applicationContext_isAvailable() {
        assertNotNull(ApplicationProvider.getApplicationContext<Context>())
    }

    @Test
    fun runsOnSdk37() {
        assertEquals(37, Build.VERSION.SDK_INT)
    }
}
