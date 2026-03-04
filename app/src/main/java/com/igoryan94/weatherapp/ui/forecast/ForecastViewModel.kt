package com.igoryan94.weatherapp.ui.forecast

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ForecastViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Здесь будет график прогноза на 7 дней"
    }
    val text: LiveData<String> = _text
}