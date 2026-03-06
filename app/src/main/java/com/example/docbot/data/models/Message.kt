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
    @Convert(converter = MessageTypeConverter::class, dbType = Int::class)
    var messageType: MessageType = MessageType.UNKNOWN,
    @Convert(converter = LocalDateTimeConverter::class, dbType = Long::class)
    var timestamp: LocalDateTime = LocalDateTime.now(),
) {
    lateinit var conversation: ToOne<Conversation>
}

enum class MessageType(val id: Int) {
    PROMPT(0), RESPONSE(1), UNKNOWN(2)
}
