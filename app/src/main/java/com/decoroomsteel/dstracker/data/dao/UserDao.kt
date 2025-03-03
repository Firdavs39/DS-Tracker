package com.decoroomsteel.dstracker.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.decoroomsteel.dstracker.data.model.User

/**
 * DAO для работы с пользователями
 */
@Dao
interface UserDao {
    /**
     * Получение пользователя по ID
     */
    @Query("SELECT * FROM users WHERE id = :userId AND active = 1")
    fun getActiveUserById(userId: String): LiveData<User?>

    /**
     * Получение пользователя по ID (синхронно)
     */
    @Query("SELECT * FROM users WHERE id = :userId AND active = 1")
    suspend fun getUserById(userId: String): User?

    /**
     * Получение всех активных пользователей
     */
    @Query("SELECT * FROM users WHERE active = 1 ORDER BY name ASC")
    fun getAllActiveUsers(): LiveData<List<User>>

    /**
     * Получение всех активных пользователей (синхронно)
     */
    @Query("SELECT * FROM users WHERE active = 1 ORDER BY name ASC")
    suspend fun getAllActiveUsersSync(): List<User>

    /**
     * Добавление пользователя
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    /**
     * Обновление данных пользователя
     */
    @Update
    suspend fun update(user: User)

    /**
     * Проверка, существует ли пользователь с указанным email
     */
    @Query("SELECT COUNT(*) FROM users WHERE email = :email AND active = 1")
    suspend fun getUserCountByEmail(email: String): Int
} 