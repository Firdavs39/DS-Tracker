package com.decoroomsteel.dstracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Сущность пользователя
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String, // Firebase Auth UID
    val email: String,
    val name: String,
    val hourlyRate: Double, // Почасовая ставка в рублях
    val isAdmin: Boolean = false, // Флаг администратора
    val active: Boolean = true // Флаг активного пользователя
) 