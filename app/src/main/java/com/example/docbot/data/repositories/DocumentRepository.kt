package com.example.docbot.data.repositories

import android.net.Uri

interface DocumentRepository {
    fun processPDF(uri: Uri)
}