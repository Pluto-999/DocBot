package com.example.docbot.data.models

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToOne

@Entity
data class DocumentChunk(
    @Id var id: Long = 0,
    var chunk: String = "",
    var embedding: FloatArray? = null,
    var documentId: Long = 0
) {
    lateinit var document: ToOne<Document>
}
