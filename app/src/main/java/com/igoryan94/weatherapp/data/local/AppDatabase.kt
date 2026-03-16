package com.igoryan94.weatherapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.igoryan94.weatherapp.data.local.dao.NotificationDao
import com.igoryan94.weatherapp.data.local.entity.NotificationEntity

@Database(
    entities = [WeatherEntity::class, NotificationEntity::class],
    version = 2, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Абстрактная функция для получения DAO. Room сгенерирует её реализацию автоматически.
     */
    abstract fun weatherDao(): WeatherDao

    abstract fun notificationDao(): NotificationDao
}