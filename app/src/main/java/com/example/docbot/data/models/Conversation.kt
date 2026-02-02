package com.example.docbot.data.models

import io.objectbox.annotation.Backlink
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToMany
import java.util.Date

@Entity
data class Conversation(
    @Id var id: Long = 0,
    var title: String = "",
    var favourite: Boolean = false,
    var latestMessage: Date = Date()
) {
    @Backlink(to = "conversation")
    lateinit var messages: ToMany<Message>
    @Backlink(to = "conversations")
    lateinit var documents: ToMany<Document>
}

