package com.example.docbot.data

import android.content.Context
import android.util.Log
import com.example.docbot.data.models.MyObjectBox
import io.objectbox.BoxStore
//import io.objectbox.android.Admin

object ObjectBox {
    lateinit var store: BoxStore
        private set

    fun init(context: Context) {
        store = MyObjectBox.builder()
            .androidContext(context)
            .build()
        // FOR DEVELOPMENT ONLY !!!! //
//        val started = Admin(store).start(context)
//        Log.i("ObjectBoxAdmin", "Started: $started")
        // !!!! //
    }
}