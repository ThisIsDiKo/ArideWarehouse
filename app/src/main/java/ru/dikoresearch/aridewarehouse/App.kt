package ru.dikoresearch.aridewarehouse

import android.app.Application
import androidx.work.Configuration
import ru.dikoresearch.aridewarehouse.di.AppComponent
import ru.dikoresearch.aridewarehouse.di.DaggerAppComponent
import javax.inject.Inject

class App: Application(), Configuration.Provider {
    lateinit var appComponent: AppComponent

    @Inject
    lateinit var workerConfiguration: Configuration

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.builder()
            .context(this)
            .build()

        appComponent.inject(this)

    }

    override fun getWorkManagerConfiguration(): Configuration {
        return workerConfiguration
    }
}

