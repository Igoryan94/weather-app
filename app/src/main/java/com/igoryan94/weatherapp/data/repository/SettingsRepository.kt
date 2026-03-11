package com.igoryan94.weatherapp.data.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    private val prefs: SharedPreferences
) {
    companion object {
        // Ключ, по которому мы будем сохранять и искать наш город
        private const val KEY_CITY = "saved_city"

        // Значение по умолчанию на случай, если пользователь зашел в первый раз
        private const val DEFAULT_CITY = "Moscow, Russia"
    }

    /**
     * Сохраняет выбранный город в SharedPreferences.
     * @param city Строка в формате "Город, Страна".
     */
    fun saveCity(city: String) {
        prefs.edit { putString(KEY_CITY, city) }
    }

    /**
     * Возвращает сохраненный город или значение по умолчанию.
     * @return Строка в формате "Город, Страна".
     */
    fun getCity(): String {
        return prefs.getString(KEY_CITY, DEFAULT_CITY) ?: DEFAULT_CITY
    }
}