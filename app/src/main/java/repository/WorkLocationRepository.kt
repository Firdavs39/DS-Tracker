package com.decoroomsteel.dstracker.repository

import androidx.lifecycle.LiveData
import com.decoroomsteel.dstracker.database.WorkLocationDao
import com.decoroomsteel.dstracker.model.WorkLocation

/**
 * Репозиторий для работы с рабочими локациями
 */
class WorkLocationRepository(private val locationDao: WorkLocationDao) {

    // Получить все активные локации
    val allActiveLocations: LiveData<List<WorkLocation>> = locationDao.getAllActiveLocations()

    // Получить локацию по ID
    fun getLocationById(locationId: Long): LiveData<WorkLocation> {
        return locationDao.getLocationById(locationId)
    }

    // Получить локацию по QR-коду
    suspend fun getLocationByQrCode(qrCode: String): WorkLocation? {
        return locationDao.getLocationByQrCode(qrCode)
    }

    // Добавить новую локацию
    suspend fun insertLocation(location: WorkLocation): Long {
        return locationDao.insert(location)
    }

    // Обновить данные локации
    suspend fun updateLocation(location: WorkLocation) {
        locationDao.update(location)
    }

    // Деактивировать локацию (удаление)
    suspend fun deactivateLocation(locationId: Long) {
        locationDao.deactivateLocation(locationId)
    }
}