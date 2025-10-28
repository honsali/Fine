package app.fine

import android.app.Application
import android.util.Log
import app.fine.di.AppContainer

class FineApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            Log.e("FineApplication", "Uncaught exception", throwable)
        }
        container = AppContainer(this)
    }
}
