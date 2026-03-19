package com.igoryan94.weatherapp.di

import com.igoryan94.weatherapp.data.network.WeatherApiService
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
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
        // Инициализируем перехватчик для вывода логов в консоль (Logcat)
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Создаем перехватчик для маскировки запроса под обычный браузер Chrome.
        // Это предотвращает ситуацию, когда CDN-защита сервера отклоняет или
        // задерживает запросы от стандартного клиента библиотеки OkHttp.
        val userAgentInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val requestWithUserAgent = originalRequest.newBuilder()
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                )
                .build()
            chain.proceed(requestWithUserAgent)
        }

        // Собираем сам клиент, применяя перехватчики и устанавливая новые таймауты
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            //.addInterceptor(userAgentInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            // Время на установку соединения с сервером
            .readTimeout(15, TimeUnit.SECONDS)
            // Время ожидания получения данных (чтение ответа)
            .writeTimeout(15, TimeUnit.SECONDS)
            // Время ожидания отправки данных на сервер
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