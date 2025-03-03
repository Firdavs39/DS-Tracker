package com.decoroomsteel.dstracker.data.repository

import androidx.lifecycle.LiveData
import com.decoroomsteel.dstracker.data.dao.LocationDao
import com.decoroomsteel.dstracker.data.model.WorkLocation

/**
 * Репозиторий для работы с рабочими зонами
 */
class LocationRepository(private val locationDao: LocationDao) {
    
    /**
     * Получение локации по ID
     */
    fun getLocationById(locationId: Long): LiveData<WorkLocation?> {
        return locationDao.getLocationById(locationId)
    }
    
    /**
     * Получение локации по ID (синхронно)
     */
    suspend fun getLocationByIdSync(locationId: Long): WorkLocation? {
        return locationDao.getLocationByIdSync(locationId)
    }
    
    /**
     * Получение локации по QR-коду
     */
    suspend fun getLocationByQrCode(qrCode: String): WorkLocation? {
        return locationDao.getLocationByQrCode(qrCode)
    }
    
    /**
     * Получение всех активных локаций
     */
    fun getAllActiveLocations(): LiveData<List<WorkLocation>> {
        return locationDao.getAllActiveLocations()
    }
    
    /**
     * Получение всех активных локаций (синхронно)
     */
    suspend fun getAllActiveLocationsSync(): List<WorkLocation> {
        return locationDao.getAllActiveLocationsSync()
    }
    
    /**
     * Добавление локации
     */
    suspend fun insert(location: WorkLocation): Long {
        return locationDao.insert(location)
    }
    
    /**
     * Обновление данных локации
     */
    suspend fun update(location: WorkLocation) {
        locationDao.update(location)
    }
    
    /**
     * Проверка существования локации с указанным QR-кодом
     */
    suspend fun doesLocationExistWithQrCode(qrCode: String): Boolean {
        return locationDao.getLocationCountByQrCode(qrCode) > 0
    }
} 