package com.igoryan94.weatherapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_forecast")
data class WeatherEntity(
    @PrimaryKey val id: Int = 0, // ID всегда 0 для кэша текущей погоды главного экрана (перезапись)
    val cityName: String,
    val temperature: String,
    val condition: String,
    val date: String,
    val feelsLike: String,
    val humidity: String,
    val windSpeed: String
)