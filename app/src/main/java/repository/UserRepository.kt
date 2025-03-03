package com.decoroomsteel.dstracker.repository

import androidx.lifecycle.LiveData
import com.decoroomsteel.dstracker.database.UserDao
import com.decoroomsteel.dstracker.model.User

/**
 * Репозиторий для работы с пользователями
 */
class UserRepository(private val userDao: UserDao) {

    // Получить всех активных пользователей
    val allActiveUsers: LiveData<List<User>> = userDao.getAllActiveUsers()

    // Получить всех администраторов
    val allAdmins: LiveData<List<User>> = userDao.getAllAdmins()

    // Получить всех работников
    val allWorkers: LiveData<List<User>> = userDao.getAllWorkers()

    // Получить пользователя по ID
    fun getUserById(userId: String): LiveData<User> {
        return userDao.getUserById(userId)
    }

    // Получить активного пользователя по ID
    fun getActiveUserById(userId: String): LiveData<User> {
        return userDao.getActiveUserById(userId)
    }

    // Добавить или обновить пользователя
    suspend fun insertUser(user: User) {
        userDao.insert(user)
    }

    // Обновить данные пользователя
    suspend fun updateUser(user: User) {
        userDao.update(user)
    }

    // Обновить почасовую ставку пользователя
    suspend fun updateHourlyRate(userId: String, hourlyRate: Double) {
        userDao.updateHourlyRate(userId, hourlyRate)
    }

    // Деактивировать пользователя (удаление)
    suspend fun deactivateUser(userId: String) {
        userDao.deactivateUser(userId)
    }
}