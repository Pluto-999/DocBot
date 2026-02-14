package com.example.docbot.data.models

import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToOne
import java.time.LocalDateTime


@Entity
data class Message(
    @Id var id: Long = 0,
    var contents: String = "",
    var messageType: String = "",
    @Convert(converter = LocalDateTimeConverter::class, dbType = Long::class)
    var timestamp: LocalDateTime? = null,
    var conversationId: Long = 0
) {
    lateinit var conversation: ToOne<Conversation>
}
