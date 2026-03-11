package com.igoryan94.weatherapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.igoryan94.weatherapp.data.repository.WeatherRepository
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
    private val repository: WeatherRepository
) : ViewModel() {

    private val _weatherState = MutableLiveData<HomeWeatherState>()
    val weatherState: LiveData<HomeWeatherState> = _weatherState

    /**
     * Вызов загрузки данных из Репозитория.
     * @param city Название города.
     */
    fun fetchCurrentWeather(city: String) {
        // Показываем состояние загрузки, пока ждем ответ
        _weatherState.value = HomeWeatherState(location = "Загрузка данных...")

        viewModelScope.launch {
            try {
                // Получаем готовый UI-стейт из Репозитория (из Сети или из Кэша)
                val state = repository.getWeatherData(city)
                _weatherState.postValue(state)
            } catch (e: Exception) {
                // Сюда попадем, только если нет интернета И база данных пуста
                _weatherState.postValue(HomeWeatherState(location = "Ошибка: ${e.message}"))
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
class HomeViewModelFactory @Inject constructor(
    private val repository: WeatherRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(repository) as T
    }
}