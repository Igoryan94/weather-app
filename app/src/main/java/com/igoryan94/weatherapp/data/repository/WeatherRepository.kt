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
    suspend fun getWeatherData(city: String, isCelsius: Boolean): HomeWeatherState {
        return try {
            Log.d("WEATHER_DEBUG", "Repo: Запрос в сеть...")

            // Пытаемся получить данные из сети
            val response = apiService.getCurrentWeather(apiKey = ApiKey.KEY, city = city)

            // Сеть успешна. Маппим данные DTO в Entity для сохранения в БД
            val entityToCache = WeatherEntity(
                id = 0, // Перезаписываем всегда первую строку (для текущей погоды)
                cityName = response.location.name,
                temperature = response.current.tempC.toInt().toString(),
                condition = response.current.condition.text,
                date = response.current.lastUpdated ?: "",
                feelsLike = "${response.current.feelsLikeC.toInt()}",
                humidity = "${response.current.humidity}%",
                windSpeed = response.current.windKph.toString()
            )

            // Сохраняем в базу Room
            weatherDao.saveWeather(entityToCache)

            // Возвращаем стейт для UI, сформированный из полученных данных
            mapEntityToUiState(entityToCache, isCelsius, isCached = false)
        } catch (e: Exception) {
            // Ошибка сети. Ищем данные в БД
            val cachedEntity = weatherDao.getCachedWeather()

            if (cachedEntity != null) {
                // Если кэш есть - отдаем его, помечая, что данные устаревшие
                mapEntityToUiState(cachedEntity, isCelsius, isCached = true)
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
    suspend fun getForecastData(targetCity: String, isCelsius: Boolean): List<ForecastDayUiModel> {
        val response = apiService.getForecastWeather(
            apiKey = ApiKey.KEY,
            city = targetCity,
            days = 7
        )

        // Маппим сетевые DTO в удобные для RecyclerView UI-модели
        return response.forecast.forecastDays.map { dayDto ->
            val tempDay = if (isCelsius) {
                "${dayDto.day.maxTempC}°C"
            } else {
                "${(dayDto.day.maxTempC * 9 / 5) + 32}°F"
            }

            val tempNight = if (isCelsius) {
                "${dayDto.day.minTempC}°C"
            } else {
                "${(dayDto.day.minTempC * 9 / 5) + 32}°F"
            }

            ForecastDayUiModel(
                date = formatApiDate(dayDto.date), // TODO добавить форматирование даты (например, "12 окт, пн")
                tempDay = "${tempDay.toInt()}",
                tempNight = "Ночью: ${tempNight.toInt()}",
                condition = dayDto.day.condition.text
            )
        }
    }

    /**
     * Вспомогательная функция для преобразования сущности БД (Entity) в UI-состояние.
     * @param entity Сущность из БД.
     * @param isCached Флаг, указывающий, взяты ли данные из кэша (чтобы предупредить пользователя).
     */
    private fun mapEntityToUiState(
        entity: WeatherEntity, isCelsius: Boolean, isCached: Boolean
    ): HomeWeatherState {
        val locationText = if (isCached) "${entity.cityName} (Оффлайн)" else entity.cityName
        val tempVal = entity.temperature.toInt()
        val windVal = entity.windSpeed.toDoubleOrNull() ?: 0.0

        val displayTemp = if (isCelsius) {
            "$tempVal°C"
        } else {
            "${(tempVal * 9 / 5) + 32}°F"
        }

        val displayFeels = if (isCelsius) {
            "${entity.feelsLike}°C"
        } else {
            "${(entity.feelsLike.toInt() * 9 / 5) + 32}°F"
        }

        // Добавим и конвертацию ветра, раз мы за системный подход
        val displayWind = if (isCelsius) {
            "${(windVal / 3.6).toInt()} м/с" // км/ч в м/с
        } else {
            "${(windVal * 0.621).toInt()} mph" // км/ч в мили/ч
        }

        return HomeWeatherState(
            location = locationText,
            currentTemp = displayTemp,
            feelsLike = "Ощущается как: $displayFeels",
            condition = entity.condition,
            humidity = entity.humidity,
            windSpeed = displayWind,
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