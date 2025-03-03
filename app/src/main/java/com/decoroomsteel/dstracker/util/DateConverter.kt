package com.decoroomsteel.dstracker.util

import androidx.room.TypeConverter
import java.util.Date

/**
 * Конвертер дат для Room
 * Необходим для преобразования Date в Long и обратно при работе с базой данных
 */
class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
} 