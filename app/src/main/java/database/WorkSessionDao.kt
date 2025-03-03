package com.decoroomsteel.dstracker.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.decoroomsteel.dstracker.model.WorkSession
import java.util.*

/**
 * DAO для работы с рабочими сменами
 */
@Dao
interface WorkSessionDao {
    @Query("SELECT * FROM work_sessions ORDER BY startTime DESC")
    fun getAllSessions(): LiveData<List<WorkSession>>

    @Query("SELECT * FROM work_sessions WHERE userId = :userId ORDER BY startTime DESC")
    fun getSessionsByUserId(userId: String): LiveData<List<WorkSession>>

    @Query("SELECT * FROM work_sessions WHERE locationId = :locationId ORDER BY startTime DESC")
    fun getSessionsByLocationId(locationId: Long): LiveData<List<WorkSession>>

    @Query("SELECT * FROM work_sessions WHERE userId = :userId AND endTime IS NULL LIMIT 1")
    suspend fun getActiveSessionForUser(userId: String): WorkSession?

    @Query("SELECT * FROM work_sessions WHERE startTime BETWEEN :startDate AND :endDate ORDER BY startTime")
    fun getSessionsInDateRange(startDate: Date, endDate: Date): LiveData<List<WorkSession>>

    @Insert
    suspend fun insert(session: WorkSession): Long

    @Update
    suspend fun update(session: WorkSession)

    @Query("UPDATE work_sessions SET endTime = :endTime, endedByAdmin = :endedByAdmin WHERE id = :sessionId")
    suspend fun endSession(sessionId: Long, endTime: Date, endedByAdmin: Boolean)
}