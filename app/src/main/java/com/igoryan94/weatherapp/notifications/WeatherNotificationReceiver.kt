package com.igoryan94.weatherapp.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.igoryan94.weatherapp.WeatherApplication
import com.igoryan94.weatherapp.data.local.dao.NotificationDao
import com.igoryan94.weatherapp.data.repository.WeatherRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Класс, который перехватывает срабатывание будильника (AlarmManager).
 */
class WeatherNotificationReceiver : BroadcastReceiver() {

    // Внедряем зависимости. Dagger должен уметь инжектить в этот класс.
    @Inject
    lateinit var repository: WeatherRepository

    @Inject
    lateinit var notificationDao: NotificationDao

    // Создаем область видимости корутин для фоновой работы
    private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Основной метод, который вызывается системой при срабатывании будильника.
     */
    override fun onReceive(context: Context, intent: Intent) {
        // Проверяем, что это именно наш будильник
        if (intent.action != "com.igoryan94.weatherapp.ACTION_DAILY_WEATHER") return

        // Выполняем инъекцию зависимостей
        (context.applicationContext as WeatherApplication).appComponent.inject(this)

        // goAsync() позволяет Receiver'у жить дольше, пока мы делаем сетевой запрос
        val pendingResult = goAsync()

        receiverScope.launch {
            try {
                // 1. Достаем настройки из БД (чтобы узнать город)
                val settings = notificationDao.getNotificationSettings()

                if (settings != null && settings.isEnabled) {
                    // 2. Делаем запрос в сеть за свежей погодой
                    val weatherState = repository.getWeatherData(
                        settings.cityName,
                        settings.isCelsius
                    )

                    // 3. Формируем текст уведомления
                    val title = "Доброе утро! Погода в ${settings.cityName}"
                    val text =
                        "${weatherState.currentTemp}, ${weatherState.forecastDays.getOrNull(0) ?: ""}"

                    // 4. Показываем уведомление
                    showNotification(context, title, text)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                // Обязательно сообщаем системе, что фоновая работа завершена
                pendingResult.finish()
            }
        }
    }

    /**
     * Создает и отображает системное уведомление.
     */
    private fun showNotification(context: Context, title: String, content: String) {
        val channelId = "weather_daily_channel"
        val notificationManager = NotificationManagerCompat.from(context)

        // Для Android 8.0 (Oreo) и выше обязательно нужен канал уведомлений
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Ежедневный прогноз",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Уведомления с утренним прогнозом погоды"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Строим само уведомление
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Замени на свою иконку солнца/облака
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        // Проверяем разрешение перед отправкой (обязательно для Android 13+)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 2001
    }
}