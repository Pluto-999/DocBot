package com.example.docbot.data.models

import io.objectbox.annotation.Entity
import io.objectbox.annotation.HnswIndex
import io.objectbox.annotation.Id
import io.objectbox.relation.ToOne

@Entity
data class DocumentChunk(
    @Id var id: Long = 0,
    var chunk: String = "",
    @HnswIndex(dimensions = 768, neighborsPerNode = 50, indexingSearchCount = 200)
    var embedding: FloatArray? = null,
) {
    lateinit var document: ToOne<Document>
}
