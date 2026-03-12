package com.example.docbot

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.docbot.data.ObjectBox
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App: Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory
    override lateinit var workManagerConfiguration: Configuration

    override fun onCreate() {
        super.onCreate()
        ObjectBox.init(this)
        workManagerConfiguration = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }
}