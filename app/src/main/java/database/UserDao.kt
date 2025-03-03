package com.decoroomsteel.dstracker.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.decoroomsteel.dstracker.model.User

/**
 * DAO для работы с пользователями
 */
@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE active = 1 ORDER BY name")
    fun getAllActiveUsers(): LiveData<List<User>>

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: String): LiveData<User>

    @Query("SELECT * FROM users WHERE id = :userId AND active = 1")
    fun getActiveUserById(userId: String): LiveData<User>

    @Query("SELECT * FROM users WHERE isAdmin = 1 AND active = 1")
    fun getAllAdmins(): LiveData<List<User>>

    @Query("SELECT * FROM users WHERE isAdmin = 0 AND active = 1")
    fun getAllWorkers(): LiveData<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Update
    suspend fun update(user: User)

    @Query("UPDATE users SET hourlyRate = :hourlyRate WHERE id = :userId")
    suspend fun updateHourlyRate(userId: String, hourlyRate: Double)

    @Query("UPDATE users SET active = 0 WHERE id = :userId")
    suspend fun deactivateUser(userId: String)
}