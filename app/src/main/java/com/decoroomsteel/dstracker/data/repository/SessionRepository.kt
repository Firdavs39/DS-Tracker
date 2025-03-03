package com.decoroomsteel.dstracker.data.repository

import androidx.lifecycle.LiveData
import com.decoroomsteel.dstracker.data.dao.SessionDao
import com.decoroomsteel.dstracker.data.model.WorkSession
import java.util.Date

/**
 * Репозиторий для работы с рабочими сменами
 */
class SessionRepository(private val sessionDao: SessionDao) {
    
    /**
     * Получение смены по ID
     */
    fun getSessionById(sessionId: Long): LiveData<WorkSession?> {
        return sessionDao.getSessionById(sessionId)
    }
    
    /**
     * Получение активной смены пользователя
     */
    fun getActiveSessionForUser(userId: String): LiveData<WorkSession?> {
        return sessionDao.getActiveSessionForUser(userId)
    }
    
    /**
     * Получение активной смены пользователя (синхронно)
     */
    suspend fun getActiveSessionForUserSync(userId: String): WorkSession? {
        return sessionDao.getActiveSessionForUserSync(userId)
    }
    
    /**
     * Получение всех активных смен
     */
    fun getAllActiveSessions(): LiveData<List<WorkSession>> {
        return sessionDao.getAllActiveSessions()
    }
    
    /**
     * Получение всех смен пользователя
     */
    fun getSessionsForUser(userId: String): LiveData<List<WorkSession>> {
        return sessionDao.getSessionsForUser(userId)
    }
    
    /**
     * Получение всех завершенных смен пользователя для указанного периода
     */
    suspend fun getSessionsForPeriodByUserSync(userId: String, startDate: Date, endDate: Date): List<WorkSession> {
        return sessionDao.getSessionsForPeriodByUserSync(userId, startDate, endDate)
    }
    
    /**
     * Получение всех смен для указанной локации за период
     */
    suspend fun getSessionsForPeriodByLocationSync(locationId: Long, startDate: Date, endDate: Date): List<WorkSession> {
        return sessionDao.getSessionsForPeriodByLocationSync(locationId, startDate, endDate)
    }
    
    /**
     * Получение всех смен за указанный период
     */
    suspend fun getSessionsForPeriodSync(startDate: Date, endDate: Date): List<WorkSession> {
        return sessionDao.getSessionsForPeriodSync(startDate, endDate)
    }
    
    /**
     * Добавление смены
     */
    suspend fun insert(session: WorkSession): Long {
        return sessionDao.insert(session)
    }
    
    /**
     * Обновление данных смены
     */
    suspend fun update(session: WorkSession) {
        sessionDao.update(session)
    }
} 