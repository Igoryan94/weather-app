package com.igoryan94.weatherapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [WeatherEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Абстрактная функция для получения DAO. Room сгенерирует её реализацию автоматически.
     */
    abstract fun weatherDao(): WeatherDao
}