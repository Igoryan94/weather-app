package com.igoryan94.weatherapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_cache")
data class WeatherEntity(
    @PrimaryKey val id: Int = 0, // У нас всегда одна запись с актуальной погодой
    val cityName: String,
    val temperature: String,
    val tempRange: String,
    val humidity: Int,
    val windSpeed: Double,
    val lastUpdated: Long // Время сохранения для проверки актуальности
)