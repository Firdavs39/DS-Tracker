package com.decoroomsteel.dstracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Сущность рабочей смены
 */
@Entity(tableName = "work_sessions")
data class WorkSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String, // ID пользователя (Firebase UID)
    val locationId: Long, // ID рабочей зоны
    val startTime: Date, // Время начала смены
    val endTime: Date? = null, // Время окончания смены (null для активных смен)
    val hourlyRate: Double, // Ставка на момент начала смены
    val startedByAdmin: Boolean = false, // Начата ли смена админом
    val endedByAdmin: Boolean = false // Завершена ли смена админом
) {
    /**
     * Получение продолжительности смены в часах
     */
    fun getDuration(): Double {
        val end = endTime ?: Date() // Если смена активна, используем текущее время
        val durationMillis = end.time - startTime.time
        val durationHours = durationMillis / (1000.0 * 60.0 * 60.0)
        return (durationHours * 100).toInt() / 100.0 // Округление до 2 знаков
    }

    /**
     * Расчет заработка за смену
     */
    fun getEarnings(): Double {
        val durationHours = getDuration()
        return (durationHours * hourlyRate * 100).toInt() / 100.0 // Округление до 2 знаков
    }

    /**
     * Проверка, активна ли смена
     */
    fun isActive(): Boolean {
        return endTime == null
    }
}