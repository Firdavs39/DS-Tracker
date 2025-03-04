package com.decoroomsteel.dstracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.decoroomsteel.dstracker.data.dao.LocationDao
import com.decoroomsteel.dstracker.data.dao.SessionDao
import com.decoroomsteel.dstracker.data.dao.UserDao
import com.decoroomsteel.dstracker.data.model.User
import com.decoroomsteel.dstracker.data.model.WorkLocation
import com.decoroomsteel.dstracker.data.model.WorkSession
import com.decoroomsteel.dstracker.util.DateConverter

/**
 * Основной класс базы данных Room
 */
@Database(
    entities = [User::class, WorkLocation::class, WorkSession::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    /**
     * DAO для работы с пользователями
     */
    abstract fun userDao(): UserDao

    /**
     * DAO для работы с рабочими зонами
     */
    abstract fun locationDao(): LocationDao

    /**
     * DAO для работы с рабочими сменами
     */
    abstract fun sessionDao(): SessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ds_tracker_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}