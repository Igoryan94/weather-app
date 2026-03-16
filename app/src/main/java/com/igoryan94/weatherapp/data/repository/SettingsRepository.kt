package com.igoryan94.weatherapp.data.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    private val prefs: SharedPreferences
) {
    companion object {
        const val KEY_THEME = "pref_theme" // 0 - System, 1 - Light, 2 - Dark
        const val KEY_UNITS = "pref_units" // "metric" или "imperial"

        // Ключ, по которому мы будем сохранять и искать наш город
        const val KEY_CITY = "saved_city"

        // Значение по умолчанию на случай, если пользователь зашел в первый раз
        private const val DEFAULT_CITY = "Moscow, Russia"

        //
        private const val GPS_STATE = "is_getting_weather_for_current_location"

        //
        const val THEME_SYSTEM = 0
        const val THEME_LIGHT = 1
        const val THEME_DARK = 2
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

    /**
     * Сохраняет состояние переключателя GPS.
     * @param isEnabled true - если GPS включен, false - если выключен.
     */
    fun saveGpsState(isEnabled: Boolean) {
        prefs.edit { putBoolean(GPS_STATE, isEnabled) }
    }

    /**
     * Возвращает сохраненное состояние переключателя GPS.
     * По умолчанию возвращает false (выключено).
     */
    fun isGpsEnabled(): Boolean {
        return prefs.getBoolean(GPS_STATE, false)
    }

    // Работа с темой
    fun getThemeMode(): Int = prefs.getInt(KEY_THEME, THEME_SYSTEM)
    fun saveThemeMode(mode: Int) = prefs.edit { putInt(KEY_THEME, mode) }

    fun getUnits(): String = prefs.getString(KEY_UNITS, "metric") ?: "metric"
    fun saveUnits(units: String) = prefs.edit { putString(KEY_UNITS, units) }

    /**
     * Регистрирует слушателя изменений в SharedPreferences.
     */
    fun registerListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    /**
     * Отписывает слушателя.
     */
    fun unregisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }
}