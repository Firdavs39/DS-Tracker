package com.decoroomsteel.dstracker.repository

import androidx.lifecycle.LiveData
import com.decoroomsteel.dstracker.database.WorkSessionDao
import com.decoroomsteel.dstracker.model.WorkSession
import java.util.*

/**
 * Репозиторий для работы с рабочими сменами
 */
class WorkSessionRepository(private val sessionDao: WorkSessionDao) {

    // Получить все смены
    val allSessions: LiveData<List<WorkSession>> = sessionDao.getAllSessions()

    // Получить смены по ID пользователя
    fun getSessionsByUserId(userId: String): LiveData<List<WorkSession>> {
        return sessionDao.getSessionsByUserId(userId)
    }

    // Получить смены по ID локации
    fun getSessionsByLocationId(locationId: Long): LiveData<List<WorkSession>> {
        return sessionDao.getSessionsByLocationId(locationId)
    }

    // Получить активную смену пользователя
    suspend fun getActiveSessionForUser(userId: String): WorkSession? {
        return sessionDao.getActiveSessionForUser(userId)
    }

    // Получить смены в диапазоне дат
    fun getSessionsInDateRange(startDate: Date, endDate: Date): LiveData<List<WorkSession>> {
        return sessionDao.getSessionsInDateRange(startDate, endDate)
    }

    // Начать новую смену
    suspend fun startSession(session: WorkSession): Long {
        return sessionDao.insert(session)
    }

    // Обновить данные смены
    suspend fun updateSession(session: WorkSession) {
        sessionDao.update(session)
    }

    // Завершить смену
    suspend fun endSession(sessionId: Long, endTime: Date, endedByAdmin: Boolean) {
        sessionDao.endSession(sessionId, endTime, endedByAdmin)
    }
}