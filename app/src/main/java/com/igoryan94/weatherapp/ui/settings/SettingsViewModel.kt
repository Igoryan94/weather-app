package com.igoryan94.weatherapp.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {

    // Состояние: включена ли геолокация
    private val _useLocation = MutableLiveData<Boolean>().apply { value = true }
    val useLocation: LiveData<Boolean> = _useLocation

    // Выбранный город (по умолчанию можно поставить пустую строку или дефолтный)
    private val _selectedCity = MutableLiveData<String>().apply { value = "Moscow" }
    val selectedCity: LiveData<String> = _selectedCity

    fun setUseLocation(use: Boolean) {
        _useLocation.value = use
    }

    fun setSelectedCity(city: String) {
        _selectedCity.value = city
        // Если пользователь выбрал город вручную, автоматически отключаем GPS
        _useLocation.value = false
    }
}