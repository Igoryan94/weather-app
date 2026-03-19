package com.igoryan94.weatherapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.igoryan94.weatherapp.data.local.entity.NotificationEntity

/**
 * Интерфейс (Data Access Object) для работы с таблицей уведомлений.
 */
@Dao
interface NotificationDao {

    /**
     * Сохраняет или обновляет настройки уведомления.
     * @param notificationEntity Объект с настройками.
     * OnConflictStrategy.REPLACE означает, что если запись с id=1 уже есть, она перезапишется новой.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveNotificationSettings(notificationEntity: NotificationEntity)

    /**
     * Извлекает текущие настройки уведомления из базы.
     * @return Объект NotificationEntity или null, если пользователь еще ничего не настраивал.
     */
    @Query("SELECT * FROM notifications WHERE id = 1")
    suspend fun getNotificationSettings(): NotificationEntity?
}