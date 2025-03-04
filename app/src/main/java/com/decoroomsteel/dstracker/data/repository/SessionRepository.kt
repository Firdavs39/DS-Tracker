package com.decoroomsteel.dstracker.data.repository

import androidx.lifecycle.LiveData
import com.decoroomsteel.dstracker.data.dao.SessionDao
import com.decoroomsteel.dstracker.data.model.WorkSession
import java.util.*

/**
 * Репозиторий для работы с рабочими сменами
 */
class SessionRepository(private val sessionDao: SessionDao) {
    
    // Получить все сессии
    val allSessions: LiveData<List<WorkSession>> = sessionDao.getAllSessions()
    
    // Получить сессии по ID пользователя
    fun getSessionsByUserId(userId: String): LiveData<List<WorkSession>> {
        return sessionDao.getSessionsForUser(userId)
    }
    
    // Получить сессии для пользователя
    fun getSessionsForUser(userId: String): LiveData<List<WorkSession>> {
        return sessionDao.getSessionsForUser(userId)
    }
    
    // Получить смены по ID локации
    fun getSessionsByLocationId(locationId: Long): LiveData<List<WorkSession>> {
        return sessionDao.getSessionsByLocationId(locationId)
    }
    
    // Получить активную смену пользователя
    fun getActiveSessionForUser(userId: String): LiveData<WorkSession?> {
        return sessionDao.getActiveSessionForUser(userId)
    }
    
    // Получить активную смену пользователя (синхронно)
    suspend fun getActiveSessionForUserSync(userId: String): WorkSession? {
        return sessionDao.getActiveSessionForUserSync(userId)
    }
    
    // Получить смены в указанном диапазоне дат
    suspend fun getSessionsInDateRange(startDate: Date, endDate: Date): List<WorkSession> {
        return sessionDao.getSessionsForPeriodSync(startDate, endDate)
    }
    
    // Начать новую смену
    suspend fun startSession(session: WorkSession): Long {
        return sessionDao.insert(session)
    }
    
    // Обновить смену
    suspend fun update(session: WorkSession) {
        sessionDao.update(session)
    }
    
    // Вставить новую сессию
    suspend fun insert(session: WorkSession): Long {
        return sessionDao.insert(session)
    }
    
    // Завершить смену
    suspend fun endSession(sessionId: Long, endTime: Date, endedByAdmin: Boolean = false) {
        val session = sessionDao.getSessionByIdSync(sessionId)
        if (session != null) {
            val updatedSession = session.copy(endTime = endTime, endedByAdmin = endedByAdmin)
            sessionDao.update(updatedSession)
        }
    }
} 