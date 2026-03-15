package com.igoryan94.weatherapp.data.model

import com.google.gson.annotations.SerializedName

/**
 * Объект города, возвращаемый при поиске.
 */
data class SearchCityDTO(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("region") val region: String,
    @SerializedName("country") val country: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double,
    @SerializedName("url") val url: String
)