package com.example.docbot.data.models

import io.objectbox.converter.PropertyConverter

class ProcessingStatusConverter: PropertyConverter<ProcessingStatus?, Int?> {

    override fun convertToEntityProperty(databaseValue: Int?): ProcessingStatus? {
        if (databaseValue == null) {
            return ProcessingStatus.QUEUED
        }
        for (status in ProcessingStatus.entries) {
            if (status.id == databaseValue) {
                return status
            }
        }
        return ProcessingStatus.QUEUED
    }

    override fun convertToDatabaseValue(entityProperty: ProcessingStatus?): Int? {
        return entityProperty?.id
    }

}