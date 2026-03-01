package com.example.docbot.data.models

import io.objectbox.converter.PropertyConverter

class MessageTypeConverter : PropertyConverter<MessageType?, Int?> {

    override fun convertToEntityProperty(databaseValue: Int?): MessageType? {
        if (databaseValue == null) {
            return MessageType.UNKNOWN
        }
        for (type in MessageType.entries) {
            if (type.id == databaseValue) {
                return type
            }
        }
        return MessageType.UNKNOWN
    }

    override fun convertToDatabaseValue(entityProperty: MessageType?): Int? {
        return entityProperty?.id
    }

}