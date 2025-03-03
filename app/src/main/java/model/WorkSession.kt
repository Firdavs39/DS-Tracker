package com.decoroomsteel.dstracker.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.*

/**
 * Модель рабочей смены
 */
@Entity(
    tableName = "work_sessions",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = WorkLocation::class,
            parentColumns = ["id"],
            childColumns = ["locationId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WorkSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,           // ID смены
    val userId: String,         // ID пользователя
    val locationId: Long,       // ID рабочей локации
    val startTime: Date,        // Время начала
    val endTime: Date? = null,  // Время окончания (null если смена активна)
    val hourlyRate: Double,     // Почасовая ставка на момент начала смены
    val startedByAdmin: Boolean = false, // Начата ли смена администратором
    val endedByAdmin: Boolean = false,   // Завершена ли смена администратором
    val notes: String = ""      // Примечания
) {
    // Рассчитать длительность смены в часах
    fun getDuration(): Double {
        if (endTime == null) return 0.0

        val durationInMillis = endTime.time - startTime.time
        return durationInMillis / (1000.0 * 60 * 60) // Перевод миллисекунд в часы
    }

    // Рассчитать заработок за смену
    fun getEarnings(): Double {
        return getDuration() * hourlyRate
    }
}