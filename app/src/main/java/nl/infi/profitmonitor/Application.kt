package nl.infi.profitmonitor

import android.app.Application
import timber.log.Timber

class Application : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Timber.i("We're off!")
    }
}