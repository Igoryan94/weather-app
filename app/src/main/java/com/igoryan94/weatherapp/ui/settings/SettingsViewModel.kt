package com.igoryan94.weatherapp.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.igoryan94.weatherapp.data.local.dao.NotificationDao
import com.igoryan94.weatherapp.data.local.entity.NotificationEntity
import com.igoryan94.weatherapp.data.repository.SettingsRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

// Добавляем инъекцию SettingsRepository
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val notificationDao: NotificationDao
) : ViewModel() {

    // LiveData для передачи настроек уведомлений в UI
    private val _notificationSettings = MutableLiveData<NotificationEntity?>()
    val notificationSettings: LiveData<NotificationEntity?> = _notificationSettings

    // LiveData для передачи состояния переключателя GPS в UI
    private val _isGpsEnabled = MutableLiveData<Boolean>()
    val isGpsEnabled: LiveData<Boolean> = _isGpsEnabled

    // LiveData для работы с темой
    private val _themeMode = MutableLiveData<Int>()
    val themeMode: LiveData<Int> = _themeMode

    private val _text = MutableLiveData<String>()
    val text: LiveData<String> = _text

    init {
        // При создании ViewModel отображаем текущий сохраненный город
        _text.value = "Выбранный город: ${settingsRepository.getCity()}"
    }

    // Выбранный город (по умолчанию можно поставить пустую строку или дефолтный)
    private val _selectedCity = MutableLiveData<String>()
        .apply { value = settingsRepository.getCity() }
    val selectedCity: LiveData<String> = _selectedCity

    /**
     * Метод вызывается, когда пользователь выбирает город в списке.
     */
    fun setSelectedCity(city: String) {
        // 1. Сохраняем в постоянную память
        settingsRepository.saveCity(city)
        // 2. Обновляем UI настроек
        _text.value = "Выбранный город: $city"
    }

    /**
     * Загружает все сохраненные настройки (уведомления и GPS) при открытии экрана.
     */
    fun loadSettings() {
        viewModelScope.launch {
            // Читаем данные из базы (Room)
            val entity = notificationDao.getNotificationSettings()
            _notificationSettings.postValue(entity)

            // Читаем данные из SharedPreferences
            _isGpsEnabled.postValue(settingsRepository.isGpsEnabled())
        }
    }

    /**
     * Сохраняет новые параметры уведомления в базу данных.
     * @param hour Час (0-23).
     * @param minute Минута (0-59).
     * @param cityName Название города для прогноза.
     * @param isEnabled Состояние переключателя.
     */
    fun saveNotificationSettings(hour: Int, minute: Int, cityName: String, isEnabled: Boolean) {
        viewModelScope.launch {
            val entity = NotificationEntity(
                id = 1,
                hour = hour,
                minute = minute,
                cityName = cityName,
                isEnabled = isEnabled
            )
            // Сохраняем в Room
            notificationDao.saveNotificationSettings(entity)
            // Обновляем UI
            _notificationSettings.postValue(entity)
        }
    }

    /**
     * Сохраняет состояние функции определения по GPS.
     */
    fun setGpsState(isEnabled: Boolean) {
        settingsRepository.saveGpsState(isEnabled)
        _isGpsEnabled.value = isEnabled
    }

    /**
     * Сохраняет название города или координаты (строку "lat,lon") как текущую локацию.
     */
    fun saveCity(city: String) {
        settingsRepository.saveCity(city)
    }

    /**
     * Получает текущий сохраненный город для отображения в UI.
     */
    fun getCurrentCity(): String {
        return settingsRepository.getCity()
    }

    /**
     * Сохраняет выбранную тему и уведомляет UI (через репозиторий/префы).
     */
    fun saveThemeMode(mode: Int) {
        settingsRepository.saveThemeMode(mode)
        // Если у тебя есть LiveData для темы, можно обновить и её
    }

    /**
     * Получает текущую сохраненную тему для установки начального состояния RadioButton.
     */
    fun getSavedThemeMode(): Int {
        return settingsRepository.getThemeMode()
    }
}

@Suppress("UNCHECKED_CAST")
class SettingsViewModelFactory @Inject constructor(
    private val repository: SettingsRepository,
    private val notificationDao: NotificationDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(repository, notificationDao) as T
    }
}