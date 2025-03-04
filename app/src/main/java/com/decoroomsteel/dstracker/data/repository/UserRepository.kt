package com.decoroomsteel.dstracker.data.repository

import androidx.lifecycle.LiveData
import com.decoroomsteel.dstracker.data.dao.UserDao
import com.decoroomsteel.dstracker.data.model.User

/**
 * Репозиторий для работы с пользователями
 */
class UserRepository(private val userDao: UserDao) {
    
    /**
     * Получение пользователя по ID
     */
    fun getActiveUserById(userId: String): LiveData<User?> {
        return userDao.getActiveUserById(userId)
    }
    
    /**
     * Получение пользователя по ID (синхронно)
     */
    suspend fun getUserById(userId: String): User? {
        return userDao.getUserById(userId)
    }
    
    /**
     * Получение всех активных пользователей
     */
    fun getAllActiveUsers(): LiveData<List<User>> {
        return userDao.getAllActiveUsers()
    }
    
    /**
     * Получение всех активных пользователей (синхронно)
     */
    suspend fun getAllActiveUsersSync(): List<User> {
        return userDao.getAllActiveUsersSync()
    }
    
    /**
     * Добавление пользователя
     */
    suspend fun insert(user: User) {
        userDao.insert(user)
    }
    
    /**
     * Обновление данных пользователя
     */
    suspend fun update(user: User) {
        userDao.update(user)
    }
    
    /**
     * Проверка, существует ли пользователь с указанным email
     */
    suspend fun doesUserExistWithEmail(email: String): Boolean {
        return userDao.getUserCountByEmail(email) > 0
    }

    // Получить всех пользователей
    val allUsers: LiveData<List<User>> = userDao.getAllUsers()

    // Получить всех работников (не админов)
    val allWorkers: LiveData<List<User>> = userDao.getAllWorkers()

    // Получить пользователя по email
    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }

    // Удалить пользователя
    suspend fun delete(user: User) {
        userDao.delete(user)
    }

    // Проверить, существует ли уже пользователь с таким email
    suspend fun isEmailExists(email: String): Boolean {
        return userDao.getUserByEmail(email) != null
    }

    // Проверить, существует ли уже пользователь с таким телефоном
    suspend fun isPhoneExists(phone: String): Boolean {
        return userDao.getUserByPhone(phone) != null
    }
} 