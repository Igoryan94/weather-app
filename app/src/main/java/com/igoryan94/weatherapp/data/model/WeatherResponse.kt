package com.igoryan94.weatherapp.data.model

import com.google.gson.annotations.SerializedName

/**
 * Корневой объект ответа WeatherAPI.
 */
data class WeatherResponse(
    @SerializedName("location") val location: LocationDTO,
    @SerializedName("current") val current: CurrentDTO
)

/**
 * Объект с данными о местоположении.
 */
data class LocationDTO(
    @SerializedName("name") val name: String,
    @SerializedName("country") val country: String
)

/**
 * Объект с текущими погодными данными.
 */
data class CurrentDTO(
    @SerializedName("temp_c") val tempC: Double,
    @SerializedName("condition") val condition: ConditionDTO,
    @SerializedName("wind_kph") val windKph: Double,
    @SerializedName("humidity") val humidity: Int,
    @SerializedName("feelslike_c") val feelsLikeC: Double
)

/**
 * Объект описания состояния погоды (текст и иконка).
 */
data class ConditionDTO(
    @SerializedName("text") val text: String,
    @SerializedName("icon") val icon: String
)