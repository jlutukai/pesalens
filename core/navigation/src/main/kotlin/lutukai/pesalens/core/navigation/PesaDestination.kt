package lutukai.pesalens.core.navigation

import androidx.navigation3.runtime.NavKey

/**
 * A destination on PesaLens' Navigation 3 back stack. Feature `api` modules declare theirs as
 * `@Serializable` objects or data classes so the back stack survives process death.
 */
interface PesaDestination : NavKey
