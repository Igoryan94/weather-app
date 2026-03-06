package com.igoryan94.weatherapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igoryan94.weatherapp.data.local.ApiKey
import com.igoryan94.weatherapp.data.network.WeatherApiService
import kotlinx.coroutines.launch
import javax.inject.Inject

// Вспомогательный класс для состояния экрана
data class HomeWeatherState(
    val location: String = "",
    val currentTemp: String = "",
    val tempRange: String = "",
    val humidity: String = "",
    val windSpeed: String = "",
    val forecastDays: List<String> = emptyList()
)

class HomeViewModel @Inject constructor(
    private val apiService: WeatherApiService
) : ViewModel() {

    // Состояние экрана, которое мы определили ранее
    private val _weatherState = MutableLiveData<HomeWeatherState>()
    val weatherState: LiveData<HomeWeatherState> = _weatherState

    /**
     * Функция запроса данных о погоде.
     * @param city Название города для поиска.
     */
    fun fetchCurrentWeather(city: String) {
        viewModelScope.launch {
            try {
                // Вызов сетевого запроса (suspend функция)
                val response = apiService.getCurrentWeather(
                    apiKey = ApiKey.KEY,
                    city = city
                )

                // Преобразование данных из API (DTO) в состояние UI
                _weatherState.postValue(
                    HomeWeatherState(
                        location = response.location.name,
                        currentTemp = "${response.current.tempC.toInt()}°C",
                        tempRange = "Ощущается как: ${response.current.feelsLikeC.toInt()}°C",
                        humidity = "Влажность: ${response.current.humidity}%",
                        windSpeed = "Ветер: ${(response.current.windKph / 3.6).toInt()} м/с", // перевод км/ч в м/с
                        forecastDays = listOf(
                            "Состояние: ${response.current.condition.text}",
                            "",
                            ""
                        )
                    )
                )
            } catch (e: Exception) {
                // В случае ошибки (например, нет интернета) обновляем заголовок
                _weatherState.postValue(HomeWeatherState(location = "Ошибка загрузки"))
            }
        }
    }
}