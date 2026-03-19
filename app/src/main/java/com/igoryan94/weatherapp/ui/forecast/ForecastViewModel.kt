package com.igoryan94.weatherapp.ui.forecast

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.igoryan94.weatherapp.data.repository.SettingsRepository
import com.igoryan94.weatherapp.data.repository.WeatherRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class ForecastViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // Используем один LiveData для всего состояния экрана
    private val _state = MutableLiveData<ForecastState>()
    val state: LiveData<ForecastState> = _state

    /**
     * Загрузка реальных данных из сети.
     */
    fun loadForecastData() {
        viewModelScope.launch {
            // Отметка о начале операции - загрузка
            _state.value = ForecastState.Loading

            try {
                // Получаем локацию из памяти (например, "Moscow, Russia")
                val fullLocation = settingsRepository.getCity()
                // Парсинг города
                val cityForApi = fullLocation.substringBefore(",").trim()
                // Параметр единиц измерения
                val isCelsius = settingsRepository.getUnits() == "metric"

                val data = repository.getForecastData(cityForApi, isCelsius)

                // Отметка об успехе операции
                _state.value = ForecastState.Success(data)
            } catch (e: Exception) {
                // Обработка ошибки
                e.printStackTrace()
                _state.value = ForecastState.Error(e.message ?: "Unknown Error")
            }
        }
    }
}

/**
 * Описывает все возможные состояния экрана прогноза.
 */
sealed class ForecastState {
    object Loading : ForecastState()
    data class Success(val data: List<ForecastDayUiModel>) : ForecastState()
    data class Error(val message: String) : ForecastState()
}

@Suppress("UNCHECKED_CAST")
class ForecastViewModelFactory @Inject constructor(
    private val repository: WeatherRepository,
    private val settingsRepository: SettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ForecastViewModel(repository, settingsRepository) as T
    }
}