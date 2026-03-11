package com.igoryan94.weatherapp.di

import android.content.Context
import androidx.room.Room
import com.igoryan94.weatherapp.data.local.AppDatabase
import com.igoryan94.weatherapp.data.local.WeatherDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseModule {

    /**
     * Создает единственный экземпляр базы данных приложения.
     */
    @Provides
    @Singleton
    fun provideDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(
            context, AppDatabase::class.java, "weather_database"
        ).build()
    }

    /**
     * Предоставляет DAO для работы с таблицей погоды.
     */
    @Provides
    @Singleton
    fun provideWeatherDao(database: AppDatabase): WeatherDao {
        return database.weatherDao()
    }
}