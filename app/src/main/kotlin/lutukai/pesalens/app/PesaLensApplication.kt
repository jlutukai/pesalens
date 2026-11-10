package lutukai.pesalens.app

import android.app.Application
import lutukai.pesalens.app.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class PesaLensApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@PesaLensApplication)
            modules(appModules)
        }
    }
}
