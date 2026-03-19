package com.igoryan94.weatherapp.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    state: HomeWeatherState,
    onShareClick: () -> Unit
) {
    // Основной контейнер, занимающий весь экран
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Локация
        Text(
            text = state.location,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // Текущая температура
        Text(
            text = state.currentTemp,
            fontSize = 96.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Диапазон / Ощущается как
        Text(
            text = state.feelsLike,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Заголовок деталей
        Text(
            text = "ТЕКУЩИЕ УСЛОВИЯ",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            modifier = Modifier.align(Alignment.Start),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Блок с подробной информацией (Влажность, Ветер, Состояние)
        Column(modifier = Modifier.fillMaxWidth()) {
            WeatherDetailRow(label = "Влажность", value = state.humidity)
            HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)

            WeatherDetailRow(label = "Ветер", value = state.windSpeed)
            HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)

            // Берем состояние из первого элемента forecastDays (как было в твоем маппере)
            val condition = state.condition
            WeatherDetailRow(
                label = "Состояние",
                value = condition,
                valueColor = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Кнопка "Поделиться"
        IconButton(
            onClick = onShareClick,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Поделиться",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

/**
 * Вспомогательная функция для отрисовки строк с деталями погоды.
 */
@Composable
fun WeatherDetailRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onBackground
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}