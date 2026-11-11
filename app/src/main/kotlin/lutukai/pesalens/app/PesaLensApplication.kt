package lutukai.pesalens.app

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import lutukai.pesalens.app.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class PesaLensApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val koin = startKoin {
            androidContext(this@PesaLensApplication)
            modules(appModules)
        }.koin
        // From process start, not from an activity, so a trip to the background is noticed whatever activity exists.
        koin.get<AppLockSession>().start(ProcessLifecycleOwner.get().lifecycle)
    }
}
