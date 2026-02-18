package com.example.docbot

import android.app.Application
import com.example.docbot.data.ObjectBox
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App: Application() {
    override fun onCreate() {
        super.onCreate()
        ObjectBox.init(this)
    }
}