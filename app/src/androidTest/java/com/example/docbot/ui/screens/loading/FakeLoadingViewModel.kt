package com.example.docbot.ui.screens.loading

import java.io.File

class FakeLoadingViewModel: LoadingViewModel {

    var initialiseCalled = false
    override suspend fun initialise(filesDir: File) {
        initialiseCalled = true
    }
}