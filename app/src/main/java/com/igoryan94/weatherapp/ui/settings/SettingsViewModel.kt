package com.igoryan94.weatherapp.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.igoryan94.weatherapp.data.repository.SettingsRepository
import javax.inject.Inject

// Добавляем инъекцию SettingsRepository
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _text = MutableLiveData<String>()
    val text: LiveData<String> = _text

    init {
        // При создании ViewModel отображаем текущий сохраненный город
        _text.value = "Выбранный город: ${settingsRepository.getCity()}"
    }

    // Состояние: включена ли геолокация
    private val _useLocation = MutableLiveData<Boolean>().apply { value = true }
    val useLocation: LiveData<Boolean> = _useLocation

    // Выбранный город (по умолчанию можно поставить пустую строку или дефолтный)
    private val _selectedCity = MutableLiveData<String>().apply { value = "Moscow" }
    val selectedCity: LiveData<String> = _selectedCity

    fun setUseLocation(use: Boolean) {
        _useLocation.value = use
    }

    /**
     * Метод вызывается, когда пользователь выбирает город в списке.
     */
    fun setSelectedCity(city: String) {
        // 1. Сохраняем в постоянную память
        settingsRepository.saveCity(city)
        // 2. Обновляем UI настроек
        _text.value = "Выбранный город: $city"
    }
}

@Suppress("UNCHECKED_CAST")
class SettingsViewModelFactory @Inject constructor(
    private val repository: SettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(repository) as T
    }
}