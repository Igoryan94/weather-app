package com.igoryan94.weatherapp

import android.app.Application
import com.igoryan94.weatherapp.di.ApplicationComponent
import com.igoryan94.weatherapp.di.DaggerApplicationComponent

class WeatherApplication : Application() {
    lateinit var appComponent: ApplicationComponent

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerApplicationComponent.builder()
            .context(this)
            .build()
    }
}