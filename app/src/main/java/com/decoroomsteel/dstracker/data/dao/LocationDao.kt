package com.decoroomsteel.dstracker.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.decoroomsteel.dstracker.data.model.WorkLocation

/**
 * DAO для работы с рабочими зонами
 */
@Dao
interface LocationDao {
    /**
     * Получение локации по ID
     */
    @Query("SELECT * FROM work_locations WHERE id = :locationId AND active = 1")
    fun getLocationById(locationId: Long): LiveData<WorkLocation?>

    /**
     * Получение локации по ID (синхронно)
     */
    @Query("SELECT * FROM work_locations WHERE id = :locationId AND active = 1")
    suspend fun getLocationByIdSync(locationId: Long): WorkLocation?

    /**
     * Получение локации по QR-коду
     */
    @Query("SELECT * FROM work_locations WHERE qrCode = :qrCode AND active = 1")
    suspend fun getLocationByQrCode(qrCode: String): WorkLocation?

    /**
     * Получение всех активных локаций
     */
    @Query("SELECT * FROM work_locations WHERE active = 1 ORDER BY name ASC")
    fun getAllActiveLocations(): LiveData<List<WorkLocation>>

    /**
     * Получение всех активных локаций (синхронно)
     */
    @Query("SELECT * FROM work_locations WHERE active = 1 ORDER BY name ASC")
    suspend fun getAllActiveLocationsSync(): List<WorkLocation>

    /**
     * Добавление локации
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: WorkLocation): Long

    /**
     * Обновление данных локации
     */
    @Update
    suspend fun update(location: WorkLocation)

    /**
     * Проверка существования локации с указанным QR-кодом
     */
    @Query("SELECT COUNT(*) FROM work_locations WHERE qrCode = :qrCode AND active = 1")
    suspend fun getLocationCountByQrCode(qrCode: String): Int
} 