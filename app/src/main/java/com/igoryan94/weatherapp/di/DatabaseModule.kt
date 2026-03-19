package com.igoryan94.weatherapp.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.igoryan94.weatherapp.data.local.AppDatabase
import com.igoryan94.weatherapp.data.local.WeatherDao
import com.igoryan94.weatherapp.data.local.dao.NotificationDao
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
        )
            .fallbackToDestructiveMigration() // Позволяет не писать миграции во время разработки
            .build()
    }

    /**
     * Предоставляет DAO для работы с таблицей погоды.
     */
    @Provides
    @Singleton
    fun provideWeatherDao(database: AppDatabase): WeatherDao {
        return database.weatherDao()
    }

    // Добавь этот метод в один из твоих существующих модулей (например, DatabaseModule)
    @Provides
    @Singleton
    fun provideSharedPreferences(context: Context): SharedPreferences {
        // Создаем файл настроек с именем "weather_app_prefs"
        return context.getSharedPreferences("weather_app_prefs", Context.MODE_PRIVATE)
    }

    /**
     * Провайдер для NotificationDao.
     * @param database Экземпляр базы данных, который Dagger создаст с помощью метода выше.
     * @return Реализация NotificationDao, сгенерированная Room.
     */
    @Provides
    @Singleton
    fun provideNotificationDao(database: AppDatabase): NotificationDao {
        return database.notificationDao()
    }
}