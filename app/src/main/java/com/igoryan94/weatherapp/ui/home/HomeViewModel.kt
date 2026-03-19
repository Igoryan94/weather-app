package com.igoryan94.weatherapp.ui.home

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.igoryan94.weatherapp.data.repository.SettingsRepository
import com.igoryan94.weatherapp.data.repository.SettingsRepository.Companion.KEY_CITY
import com.igoryan94.weatherapp.data.repository.SettingsRepository.Companion.KEY_UNITS
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
    private val repository: WeatherRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _weatherState = MutableLiveData<HomeWeatherState>()
    val weatherState: LiveData<HomeWeatherState> = _weatherState

    private val preferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_UNITS || key == KEY_CITY) {
                // Если изменились единицы — просто перечитываем данные из репозитория (они возьмутся из кэша Room)
                loadWeatherForSavedCity()
            }
        }

    init {
        // 2. Подписываемся на изменения через репозиторий при создании ViewModel
        settingsRepository.registerListener(preferenceChangeListener)
    }

    override fun onCleared() {
        super.onCleared()
        // Важно отписаться, чтобы не было утечки памяти!
        settingsRepository.unregisterListener(preferenceChangeListener)
    }

    /**
     * Главный метод инициализации.
     * Достает город из памяти, форматирует его и запрашивает погоду.
     */
    fun loadWeatherForSavedCity() {
        // Получаем полную строку из памяти (например, "Moscow, Russia")
        val fullLocation = settingsRepository.getCity()

        // Парсим строку: берем всё до первой запятой и удаляем лишние пробелы.
        // Если запятой нет (например, пользователь ввел просто "Kazan"), вернет всю строку.
        val cityForApi = fullLocation.substringBefore(",").trim()

        // Вызываем метод запроса в сеть с уже отформатированным названием
        fetchCurrentWeather(cityForApi)
    }

    /**
     * Внутренний метод загрузки данных из Репозитория.
     * @param city Очищенное название города (например, "Moscow").
     */
    private fun fetchCurrentWeather(city: String) {
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
    private val repository: WeatherRepository,
    private val settingsRepository: SettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(repository, settingsRepository) as T
    }
}