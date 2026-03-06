package com.igoryan94.weatherapp.ui.forecast

// Data-класс, описывающий данные для одной строки в списке прогноза
data class ForecastDayUiModel(
    val date: String,       // Например, "Пн, 12 Окт"
    val tempDay: String,    // Например, "+18°C"
    val tempNight: String,  // Например, "+10°C"
    val condition: String   // Например, "Солнечно"
)