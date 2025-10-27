package app.fine

import android.app.Application
import app.fine.di.AppContainer

class FineApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
