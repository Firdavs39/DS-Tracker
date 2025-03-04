package com.decoroomsteel.dstracker.data.repository

import androidx.lifecycle.LiveData
import com.decoroomsteel.dstracker.data.dao.LocationDao
import com.decoroomsteel.dstracker.data.model.WorkLocation

/**
 * Репозиторий для работы с рабочими локациями
 */
class LocationRepository(private val locationDao: LocationDao) {

    // Получить все локации
    val allLocations: LiveData<List<WorkLocation>> = locationDao.getAllLocations()

    // Получить локацию по ID
    suspend fun getLocationById(locationId: Long): WorkLocation? {
        return locationDao.getLocationByIdSync(locationId)
    }

    // Получить локацию по ID (синхронно)
    suspend fun getLocationByIdSync(locationId: Long): WorkLocation? {
        return locationDao.getLocationByIdSync(locationId)
    }

    // Получить локацию по QR-коду
    suspend fun getLocationByQrCode(qrCode: String): WorkLocation? {
        return locationDao.getLocationByQrCode(qrCode)
    }

    // Добавить новую локацию
    suspend fun insert(location: WorkLocation): Long {
        return locationDao.insert(location)
    }

    // Обновить данные локации
    suspend fun update(location: WorkLocation) {
        locationDao.update(location)
    }

    // Удалить локацию
    suspend fun delete(location: WorkLocation) {
        locationDao.delete(location)
    }

    // Проверить, существует ли локация с таким QR-кодом
    suspend fun isQrCodeExists(qrCode: String): Boolean {
        return locationDao.getLocationByQrCode(qrCode) != null
    }
} 