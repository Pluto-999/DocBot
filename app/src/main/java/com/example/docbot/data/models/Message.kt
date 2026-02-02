package com.example.docbot.data.models

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToOne
import java.util.Date


@Entity
data class Message(
    @Id var id: Long = 0,
    var contents: String = "",
    var messageType: String = "",
    var timestamp: Date = Date(),
    var conversationId: Long = 0
) {
    lateinit var conversation: ToOne<Conversation>
}
