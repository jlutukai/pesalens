package lutukai.pesalens.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity

/** A [FragmentActivity] because the app lock's BiometricPrompt needs one. */
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Edge to edge from the first frame; PesaLensApp re-applies it with the bar icons matched to the app's theme.
        enableEdgeToEdge()
        setContent { PesaLensApp() }
    }
}
