package com.igoryan94.weatherapp.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Здесь будет поиск города и настройки локации"
    }
    val text: LiveData<String> = _text
}