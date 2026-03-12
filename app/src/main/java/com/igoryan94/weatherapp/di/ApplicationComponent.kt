package com.igoryan94.weatherapp.di

import android.content.Context
import com.igoryan94.weatherapp.ui.forecast.ForecastFragment
import com.igoryan94.weatherapp.ui.home.HomeFragment
import com.igoryan94.weatherapp.ui.settings.CitySearchActivity
import com.igoryan94.weatherapp.ui.settings.SettingsFragment
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [NetworkModule::class, DatabaseModule::class])
interface ApplicationComponent {

    fun inject(fragment: HomeFragment)

    fun inject(fragment: ForecastFragment)

    fun inject(fragment: SettingsFragment)
    fun inject(activity: CitySearchActivity)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun context(context: Context): Builder

        fun build(): ApplicationComponent
    }
}