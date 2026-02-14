package com.example.docbot.data.models

import io.objectbox.converter.PropertyConverter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class LocalDateTimeConverter : PropertyConverter<LocalDateTime, Long> {

    override fun convertToEntityProperty(databaseValue: Long?): LocalDateTime? {
        if (databaseValue == null) {
            return LocalDateTime.now()
        }
        return Instant.ofEpochMilli(databaseValue).atZone(ZoneId.systemDefault()).toLocalDateTime()
    }

    override fun convertToDatabaseValue(entityProperty: LocalDateTime?): Long? {
        if (entityProperty == null) {
            return System.currentTimeMillis()
        }
        return entityProperty.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}