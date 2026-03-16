package com.igoryan94.weatherapp.notifications

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

/**
 * Класс, отвечающий за постановку и отмену точных системных будильников.
 */
class WeatherAlarmScheduler(
    private val context: Context
) {
    // Получаем системный сервис AlarmManager
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Функция планирования ежедневного уведомления.
     * @param hour Час (0-23), в который должно сработать уведомление.
     * @param minute Минута (0-59), в которую должно сработать уведомление.
     */
    @SuppressLint("ScheduleExactAlarm") // Подавляем предупреждение студии, так как разрешения мы прописали
    fun scheduleDailyAlarm(hour: Int, minute: Int) {
        // 1. Создаем Intent, который указывает на наш будущий BroadcastReceiver
        val intent = Intent(context, WeatherNotificationReceiver::class.java).apply {
            // Устанавливаем стабильный action, как требовалось в задании
            action = "com.igoryan94.weatherapp.ACTION_DAILY_WEATHER"
        }

        // 2. Оборачиваем Intent в PendingIntent.
        // PendingIntent позволяет системе (AlarmManager) выполнить наш Intent от имени нашего приложения в будущем.
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            // Флаги: если такой PendingIntent уже существует, мы его обновляем. Immutable требует безопасность Android.
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Рассчитываем время для срабатывания через Calendar
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // Если выбранное время уже прошло сегодня, переносим будильник на завтра
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        // 4. Устанавливаем точный будильник.
        // setExactAndAllowWhileIdle гарантирует, что будильник сработает даже если телефон в режиме сна (Doze mode).
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, // Разбудить устройство по достижению времени
            calendar.timeInMillis,
            pendingIntent
        )
    }

    /**
     * Функция для отмены запланированного будильника.
     */
    fun cancelAlarm() {
        val intent = Intent(context, WeatherNotificationReceiver::class.java).apply {
            action = "com.igoryan94.weatherapp.ACTION_DAILY_WEATHER"
        }

        // Идентичный PendingIntent (тот же класс, request code и action)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Отменяем его в AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    companion object {
        // Уникальный ID для нашего будильника
        private const val ALARM_REQUEST_CODE = 1001
    }
}