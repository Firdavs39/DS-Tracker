package com.decoroomsteel.dstracker.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.decoroomsteel.dstracker.model.WorkLocation

/**
 * DAO для работы с рабочими локациями
 */
@Dao
interface WorkLocationDao {
    @Query("SELECT * FROM work_locations WHERE active = 1 ORDER BY name")
    fun getAllActiveLocations(): LiveData<List<WorkLocation>>

    @Query("SELECT * FROM work_locations WHERE id = :locationId")
    fun getLocationById(locationId: Long): LiveData<WorkLocation>

    @Query("SELECT * FROM work_locations WHERE qrCode = :qrCode AND active = 1")
    suspend fun getLocationByQrCode(qrCode: String): WorkLocation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: WorkLocation): Long

    @Update
    suspend fun update(location: WorkLocation)

    @Query("UPDATE work_locations SET active = 0 WHERE id = :locationId")
    suspend fun deactivateLocation(locationId: Long)
}