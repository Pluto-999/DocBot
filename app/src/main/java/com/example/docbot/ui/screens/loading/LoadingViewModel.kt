package com.example.docbot.ui.screens.loading

import java.io.File

interface LoadingViewModel {

    suspend fun initialise(filesDirectory: File)
}