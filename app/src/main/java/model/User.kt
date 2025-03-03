package com.decoroomsteel.dstracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Модель пользователя в системе
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String,         // ID пользователя (из Firebase Auth)
    val email: String,      // Email пользователя
    val name: String,       // Имя пользователя
    val isAdmin: Boolean,   // Флаг администратора
    val hourlyRate: Double, // Почасовая ставка
    val active: Boolean = true // Активен ли пользователь
)