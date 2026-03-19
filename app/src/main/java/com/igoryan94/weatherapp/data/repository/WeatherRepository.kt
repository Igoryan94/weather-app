package com.igoryan94.weatherapp.data.repository

import android.util.Log
import com.igoryan94.weatherapp.data.local.ApiKey
import com.igoryan94.weatherapp.data.local.WeatherDao
import com.igoryan94.weatherapp.data.local.WeatherEntity
import com.igoryan94.weatherapp.data.network.WeatherApiService
import com.igoryan94.weatherapp.ui.forecast.ForecastDayUiModel
import com.igoryan94.weatherapp.ui.home.HomeWeatherState
import okio.IOException
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class WeatherRepository @Inject constructor(
    private val apiService: WeatherApiService,
    private val weatherDao: WeatherDao
) {

    /**
     * Главный метод получения погоды. Инкапсулирует работу с сетью и кэшем.
     * @param city Название города для поиска.
     * @return [HomeWeatherState] готовое состояние для UI.
     */
    suspend fun getWeatherData(city: String): HomeWeatherState {
        return try {
            Log.d("WEATHER_DEBUG", "Repo: Запрос в сеть...")

            // Пытаемся получить данные из сети
            val response = apiService.getCurrentWeather(apiKey = ApiKey.KEY, city = city)

            // Сеть успешна. Маппим данные DTO в Entity для сохранения в БД
            val entityToCache = WeatherEntity(
                id = 0, // Перезаписываем всегда первую строку (для текущей погоды)
                cityName = response.location.name,
                temperature = "${response.current.tempC.toInt()}°C",
                condition = response.current.condition.text,
                date = response.current.lastUpdated ?: "",
                feelsLike = "Ощущается как: ${response.current.feelsLikeC.toInt()}°C",
                humidity = "${response.current.humidity}%",
                windSpeed = "${(response.current.windKph / 3.6).toInt()} м/с"
            )

            // Сохраняем в базу Room
            weatherDao.saveWeather(entityToCache)

            // Возвращаем стейт для UI, сформированный из полученных данных
            mapEntityToUiState(entityToCache, isCached = false)
        } catch (e: Exception) {
            // Ошибка сети. Ищем данные в БД
            val cachedEntity = weatherDao.getCachedWeather()

            if (cachedEntity != null) {
                // Если кэш есть - отдаем его, помечая, что данные устаревшие
                mapEntityToUiState(cachedEntity, isCached = true)
            } else {
                // Если и в кэше пусто (первый запуск), пробрасываем ошибку дальше в ViewModel
                val errorMessage = when (e) {
                    is IOException -> "Нет подключения к интернету"
                    is HttpException -> "Ошибка сервера: ${e.code()}"
                    else -> "Непредвиденная ошибка"
                }
                throw Exception(errorMessage)
            }
        }
    }

    /**
     * Получение прогноза на 7 дней.
     * Сначала узнаем, какой город выбран пользователем (из локальной БД).
     */
    suspend fun getForecastData(targetCity: String): List<ForecastDayUiModel> {
        val response = apiService.getForecastWeather(
            apiKey = ApiKey.KEY,
            city = targetCity,
            days = 7
        )

        // Маппим сетевые DTO в удобные для RecyclerView UI-модели
        return response.forecast.forecastDays.map { dayDto ->
            ForecastDayUiModel(
                date = formatApiDate(dayDto.date), // TODO добавить форматирование даты (например, "12 окт, пн")
                tempDay = "${dayDto.day.maxTempC.toInt()}°C",
                tempNight = "Ночью: ${dayDto.day.minTempC.toInt()}°C",
                condition = dayDto.day.condition.text
            )
        }
    }

    /**
     * Вспомогательная функция для преобразования сущности БД (Entity) в UI-состояние.
     * @param entity Сущность из БД.
     * @param isCached Флаг, указывающий, взяты ли данные из кэша (чтобы предупредить пользователя).
     */
    private fun mapEntityToUiState(entity: WeatherEntity, isCached: Boolean): HomeWeatherState {
        val locationText = if (isCached) "${entity.cityName} (Оффлайн)" else entity.cityName

        return HomeWeatherState(
            location = locationText,
            currentTemp = entity.temperature,
            tempRange = entity.feelsLike,
            humidity = entity.humidity,
            windSpeed = entity.windSpeed,
            forecastDays = emptyList()
        )
    }

    /**
     * Получение списка городов по запросу.
     */
    suspend fun searchCities(query: String): List<String> {
        return try {
            val cities = apiService.searchCity(apiKey = ApiKey.KEY, query = query)
            // Преобразуем список DTO в список строк вида "City, Country" для удобства отображения
            cities.map { "${it.name}, ${it.country}" }
        } catch (e: Exception) {
            Log.e("WEATHER_DEBUG", "Ошибка при поиске: ${e.message}")
            emptyList()
        }
    }

    /**
     * Форматирование даты для читаемого вида
     * @param dateInput Исходная дата с сервера
     */
    private fun formatApiDate(dateInput: String): String {
        // Формат, в котором дата приходит от API
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        // Формат, который мы хотим получить: "d MMMM, EE"
        // d - день (без нуля), MMMM - полное название месяца, EE - сокращенный день недели
        val outputFormat = SimpleDateFormat("d MMMM, EE", Locale("ru"))

        return try {
            val date = inputFormat.parse(dateInput)
            date?.let { outputFormat.format(it).lowercase() } ?: dateInput
        } catch (_: Exception) {
            dateInput // Если что-то пошло не так, вернем как было
        }
    }
}