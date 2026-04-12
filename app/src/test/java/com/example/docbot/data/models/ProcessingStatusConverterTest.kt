package com.example.docbot.data.models

import org.junit.Assert.*
import org.junit.Test

class ProcessingStatusConverterTest {

    private val converter = ProcessingStatusConverter()

    @Test
    fun testConvertToEntityPropertyReturnsQueuedWithNullDatabaseValue() {
        val result = converter.convertToEntityProperty(null)
        assertEquals(ProcessingStatus.QUEUED, result)
    }

    @Test
    fun testConvertToEntityPropertyReturnsQueuedWithDatabaseValueNotInProcessingStatus() {
        val result = converter.convertToEntityProperty(-1)
        assertEquals(ProcessingStatus.QUEUED, result)
    }

    @Test
    fun testConvertToEntityPropertyReturnsCorrectTypeForEachProcessingStatus() {
        for (status in ProcessingStatus.entries) {
            val result = converter.convertToEntityProperty(status.id)
            assertEquals(status, result)
        }
    }

    @Test
    fun testConvertToDatabaseValueReturnsNullWithNullEntityProperty() {
        val result = converter.convertToDatabaseValue(null)
        assertNull(result)
    }
}