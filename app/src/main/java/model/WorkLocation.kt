package com.decoroomsteel.dstracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Модель рабочей локации/объекта
 */
@Entity(tableName = "work_locations")
data class WorkLocation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,           // ID локации
    val name: String,           // Название объекта
    val address: String,        // Адрес
    val latitude: Double,       // Широта для геолокации
    val longitude: Double,      // Долгота для геолокации
    val qrCode: String,         // Уникальный код для QR
    val active: Boolean = true  // Активна ли локация
)