package ru.dikoresearch.aridewarehouse

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import ru.dikoresearch.aridewarehouse.di.AppComponent
import ru.dikoresearch.aridewarehouse.di.DaggerAppComponent
import ru.dikoresearch.aridewarehouse.domain.workers.WorkersFactory
import javax.inject.Inject

class App: Application(), Configuration.Provider {
//class App: Application(){

    lateinit var appComponent: AppComponent

    @Inject
    lateinit var workerConfiguration: Configuration


    override fun onCreate() {
        super.onCreate()

        //old var without context appComponent = DaggerAppComponent.create()

        appComponent = DaggerAppComponent.builder()
            .context(this)
            .build()

        appComponent.inject(this)


// initialize WorkManager
        //WorkManager.initialize(this, workerConfiguration)
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return workerConfiguration
    }
}

