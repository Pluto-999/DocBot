package com.example.docbot.data.models

import org.junit.Assert.*
import org.junit.Test

class MessageTypeConverterTest {

    private val converter = MessageTypeConverter()

    @Test
    fun testConvertToEntityPropertyReturnsUnknownWithNullDatabaseValue() {
        val result = converter.convertToEntityProperty(null)
        assertEquals(MessageType.UNKNOWN, result)
    }

    @Test
    fun testConvertToEntityPropertyReturnsUnknownWithDatabaseValueNotInMessageType() {
        val result = converter.convertToEntityProperty(-1)
        assertEquals(MessageType.UNKNOWN, result)
    }

    @Test
    fun testConvertToEntityPropertyReturnsCorrectTypeForEachMessageType() {
        for (type in MessageType.entries) {
            val result = converter.convertToEntityProperty(type.id)
            assertEquals(type, result)
        }
    }

    @Test
    fun testConvertToDatabaseValueReturnsNullWithNullEntityProperty() {
        val result = converter.convertToDatabaseValue(null)
        assertNull(result)
    }
}