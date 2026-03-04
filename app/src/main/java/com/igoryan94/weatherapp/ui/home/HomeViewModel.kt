package com.igoryan94.weatherapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// Вспомогательный класс для состояния экрана
data class HomeWeatherState(
    val location: String = "",
    val currentTemp: String = "",
    val tempRange: String = "",
    val humidity: String = "",
    val windSpeed: String = "",
    val forecastDays: List<String> = emptyList()
)

class HomeViewModel : ViewModel() {

    private val _weatherState = MutableLiveData<HomeWeatherState>().apply {
        value = HomeWeatherState(
            location = "Загрузка...",
            currentTemp = "--°C",
            tempRange = "от -- до --",
            humidity = "Влажность: --%",
            windSpeed = "Ветер: -- м/с",
            forecastDays = listOf("...", "...", "...")
        )
    }
    val weatherState: LiveData<HomeWeatherState> = _weatherState

    // Метод для будущего обновления данных из API
    fun updateWeather(state: HomeWeatherState) {
        _weatherState.value = state
    }
}