package com.igoryan94.weatherapp.data.network

import com.igoryan94.weatherapp.data.model.SearchCityDTO
import com.igoryan94.weatherapp.data.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    /**
     * Метод для получения текущей погоды.
     * @param apiKey Наш API ключ.
     * @param city Название города (например, "Moscow").
     * @param aqi Включение/выключение индекса качества воздуха ("yes"/"no").
     * @param lang Язык ответа (например, "ru").
     */
    @GET("v1/current.json")
    suspend fun getCurrentWeather(
        @Query("key") apiKey: String,
        @Query("q") city: String,
        @Query("aqi") aqi: String = "no",
        @Query("lang") lang: String = "ru"
    ): WeatherResponse

    /**
     * Метод поиска городов по текстовому запросу.
     */
    @GET("v1/search.json")
    suspend fun searchCity(
        @Query("key") apiKey: String,
        @Query("q") query: String
    ): List<SearchCityDTO>
}