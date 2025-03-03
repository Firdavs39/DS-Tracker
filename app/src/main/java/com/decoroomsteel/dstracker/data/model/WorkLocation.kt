package com.decoroomsteel.dstracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Сущность рабочей зоны
 */
@Entity(tableName = "work_locations")
data class WorkLocation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val qrCode: String, // Уникальный код для QR-кода
    val active: Boolean = true // Флаг активной локации
) 