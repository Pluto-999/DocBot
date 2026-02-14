package com.example.docbot.data.models

import io.objectbox.annotation.Backlink
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToMany
import java.time.LocalDateTime

@Entity
data class Conversation(
    @Id var id: Long = 0,
    var title: String = "",
    var favourite: Boolean = false,
    @Convert(converter = LocalDateTimeConverter::class, dbType = Long::class)
    var latestMessage: LocalDateTime? = null
) {
    @Backlink(to = "conversation")
    lateinit var messages: ToMany<Message>

    lateinit var documents: ToMany<Document>
}

