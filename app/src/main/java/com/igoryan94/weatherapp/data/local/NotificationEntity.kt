package com.igoryan94.weatherapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Сущность для таблицы настроек уведомлений.
 * Мы будем хранить только одну запись, поэтому ID всегда будет равен 1.
 */
@Entity(tableName = "notifications")
data class NotificationEntity(
    // Задаем фиксированный ID, так как нам нужна только одна настройка на всё приложение
    @PrimaryKey val id: Int = 1,

    // Время срабатывания в часах (от 0 до 23)
    val hour: Int,

    // Время срабатывания в минутах (от 0 до 59)
    val minute: Int,

    // Город, для которого запрашиваем прогноз утром
    val cityName: String,

    // Флаг состояния: включено ли уведомление пользователем
    val isEnabled: Boolean
)