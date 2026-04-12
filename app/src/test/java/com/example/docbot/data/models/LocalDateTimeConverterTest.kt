package com.example.docbot.data.models

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

class LocalDateTimeConverterTest {

    private val converter = LocalDateTimeConverter()

    @Test
    fun testConvertToEntityPropertyReturnsCurrentDateTimeWithNullDatabaseValue() {
        val timeBefore = LocalDateTime.now()
        val result = converter.convertToEntityProperty(null)
        val timeAfter = LocalDateTime.now()

        assertNotNull(result)

        if (result != null) {
            assertTrue(result >= timeBefore && result <= timeAfter)
        }
    }

    @Test
    fun testConvertToDatabaseValueReturnsCurrentMillisecondsWithNullEntityProperty() {
        val millisBefore = System.currentTimeMillis()
        val result = converter.convertToDatabaseValue(null)
        val millisAfter = System.currentTimeMillis()

        assertNotNull(result)
        if (result != null) {
            assertTrue(result >= millisBefore && result <= millisAfter)
        }

    }
}