package com.example.docbot.data.models

import io.objectbox.annotation.Backlink
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToMany

@Entity
data class Document(
    @Id var id: Long = 0,
    var name: String = "",
    var contentHash: String = "",
    @Convert(converter = ProcessingStatusConverter::class, dbType = Int::class)
    var processingStatus: ProcessingStatus = ProcessingStatus.QUEUED
) {
    @Backlink(to = "document")
    lateinit var documentChunks: ToMany<DocumentChunk>

    @Backlink(to = "documents")
    lateinit var conversations: ToMany<Conversation>
}

enum class ProcessingStatus(val id: Int) {
    FAILED(0), QUEUED(1), PROCESSING(2), COMPLETED(3)
}