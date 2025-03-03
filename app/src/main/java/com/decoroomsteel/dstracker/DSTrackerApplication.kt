package com.decoroomsteel.dstracker

import android.app.Application
import androidx.room.Room
import com.decoroomsteel.dstracker.data.AppDatabase
import com.decoroomsteel.dstracker.data.repository.LocationRepository
import com.decoroomsteel.dstracker.data.repository.SessionRepository
import com.decoroomsteel.dstracker.data.repository.UserRepository
import com.google.firebase.FirebaseApp

/**
 * Основной класс приложения для инициализации компонентов
 */
class DSTrackerApplication : Application() {

    // База данных Room
    private lateinit var database: AppDatabase
    
    // Репозитории для доступа к данным
    lateinit var userRepository: UserRepository
    lateinit var locationRepository: LocationRepository
    lateinit var sessionRepository: SessionRepository
    
    override fun onCreate() {
        super.onCreate()
        
        // Инициализация Firebase
        FirebaseApp.initializeApp(this)
        
        // Инициализация базы данных Room
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "ds_tracker_database"
        )
        .fallbackToDestructiveMigration()
        .build()
        
        // Инициализация репозиториев
        userRepository = UserRepository(database.userDao())
        locationRepository = LocationRepository(database.locationDao())
        sessionRepository = SessionRepository(database.sessionDao())
    }
}