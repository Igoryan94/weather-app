package com.igoryan94.weatherapp

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.igoryan94.weatherapp.data.repository.SettingsRepository
import com.igoryan94.weatherapp.data.repository.SettingsRepository.Companion.THEME_DARK
import com.igoryan94.weatherapp.data.repository.SettingsRepository.Companion.THEME_LIGHT
import com.igoryan94.weatherapp.di.ApplicationComponent
import com.igoryan94.weatherapp.di.DaggerApplicationComponent
import javax.inject.Inject

class WeatherApplication : Application() {
    lateinit var appComponent: ApplicationComponent

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate() {
        super.onCreate()

        // Внедряем зависимости
        appComponent = DaggerApplicationComponent.builder()
            .context(this)
            .build()
        appComponent.inject(this)

        // Применяем тему
        applySavedTheme()
    }

    private fun applySavedTheme() {
        val mode = settingsRepository.getThemeMode()
        AppCompatDelegate.setDefaultNightMode(
            when (mode) {
                THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }
}