package com.decoroomsteel.dstracker.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.decoroomsteel.dstracker.data.model.WorkSession
import java.util.Date

/**
 * DAO для работы с рабочими сменами
 */
@Dao
interface SessionDao {
    /**
     * Получение смены по ID
     */
    @Query("SELECT * FROM work_sessions WHERE id = :sessionId")
    fun getSessionById(sessionId: Long): LiveData<WorkSession?>

    /**
     * Получение смены по ID (синхронно)
     */
    @Query("SELECT * FROM work_sessions WHERE id = :sessionId")
    suspend fun getSessionByIdSync(sessionId: Long): WorkSession?

    /**
     * Получение активной смены пользователя
     */
    @Query("SELECT * FROM work_sessions WHERE userId = :userId AND endTime IS NULL")
    fun getActiveSessionForUser(userId: String): LiveData<WorkSession?>

    /**
     * Получение активной смены пользователя (синхронно)
     */
    @Query("SELECT * FROM work_sessions WHERE userId = :userId AND endTime IS NULL")
    suspend fun getActiveSessionForUserSync(userId: String): WorkSession?

    /**
     * Получение всех активных смен
     */
    @Query("SELECT * FROM work_sessions WHERE endTime IS NULL")
    fun getAllActiveSessions(): LiveData<List<WorkSession>>

    /**
     * Получение всех смен пользователя
     */
    @Query("SELECT * FROM work_sessions WHERE userId = :userId ORDER BY startTime DESC")
    fun getSessionsForUser(userId: String): LiveData<List<WorkSession>>

    /**
     * Получение всех завершенных смен пользователя для указанного периода
     */
    @Query("SELECT * FROM work_sessions WHERE userId = :userId AND startTime >= :startDate AND startTime <= :endDate ORDER BY startTime DESC")
    suspend fun getSessionsForPeriodByUserSync(userId: String, startDate: Date, endDate: Date): List<WorkSession>

    /**
     * Получение всех смен для указанной локации за период
     */
    @Query("SELECT * FROM work_sessions WHERE locationId = :locationId AND startTime >= :startDate AND startTime <= :endDate ORDER BY startTime DESC")
    suspend fun getSessionsForPeriodByLocationSync(locationId: Long, startDate: Date, endDate: Date): List<WorkSession>

    /**
     * Получение всех смен за указанный период
     */
    @Query("SELECT * FROM work_sessions WHERE startTime >= :startDate AND startTime <= :endDate ORDER BY startTime DESC")
    suspend fun getSessionsForPeriodSync(startDate: Date, endDate: Date): List<WorkSession>

    /**
     * Получение всех смен для указанной локации
     */
    @Query("SELECT * FROM work_sessions WHERE locationId = :locationId ORDER BY startTime DESC")
    fun getSessionsByLocationId(locationId: Long): LiveData<List<WorkSession>>

    /**
     * Добавление смены
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: WorkSession): Long

    /**
     * Обновление данных смены
     */
    @Update
    suspend fun update(session: WorkSession)

    /**
     * Получение всех смен
     */
    @Query("SELECT * FROM work_sessions ORDER BY startTime DESC")
    fun getAllSessions(): LiveData<List<WorkSession>>
} 