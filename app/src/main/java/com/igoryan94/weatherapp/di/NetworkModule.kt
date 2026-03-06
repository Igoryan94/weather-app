package com.igoryan94.weatherapp.di

import com.igoryan94.weatherapp.data.network.WeatherApiService
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
class NetworkModule {

    /**
     * Создает и настраивает OkHttpClient.
     * Здесь мы подключаем HttpLoggingInterceptor для вывода JSON и деталей запроса в Logcat.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            // Level.BODY означает, что мы хотим видеть тело запроса и ответа (весь JSON)
            // TODO убрать отладку при релизе
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    /**
     * Создание базового объекта Retrofit с привязанным OkHttpClient.
     * Мы указываем базовый URL и добавляем конвертер Gson для парсинга JSON.
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.weatherapi.com/")
            .client(okHttpClient) // Подключаем наш клиент с логгированием
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Предоставление реализации интерфейса API.
     */
    @Provides
    @Singleton
    fun provideWeatherApiService(retrofit: Retrofit): WeatherApiService {
        return retrofit.create(WeatherApiService::class.java)
    }
}