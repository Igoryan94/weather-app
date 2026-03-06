package com.igoryan94.weatherapp.ui.forecast

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ForecastViewModel : ViewModel() {
    // Хранилище списка прогнозов
    private val _forecastList = MutableLiveData<List<ForecastDayUiModel>>()
    val forecastList: LiveData<List<ForecastDayUiModel>> = _forecastList

    // Имитация загрузки данных. В будущем здесь будет вызов репозитория/Room/Retrofit
    fun loadMockData() {
        val mockData = listOf(
            ForecastDayUiModel("Завтра, 13 Мар", "+18°C", "Ночью: +10°C", "Ясно"),
            ForecastDayUiModel("Пт, 14 Мар", "+16°C", "Ночью: +9°C", "Облачно"),
            ForecastDayUiModel("Сб, 15 Мар", "+15°C", "Ночью: +8°C", "Возможен дождь"),
            ForecastDayUiModel("Вс, 16 Мар", "+20°C", "Ночью: +12°C", "Солнечно"),
            ForecastDayUiModel("Пн, 17 Мар", "+22°C", "Ночью: +14°C", "Ясно"),
            ForecastDayUiModel("Вт, 18 Мар", "+19°C", "Ночью: +11°C", "Переменная облачность"),
            ForecastDayUiModel("Ср, 19 Мар", "+17°C", "Ночью: +9°C", "Пасмурно")
        )
        _forecastList.value = mockData
    }
}