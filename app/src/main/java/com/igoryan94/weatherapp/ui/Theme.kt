package com.igoryan94.weatherapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 1. Определение цветов напрямую (цвета из ваших XML)
val Purple500 = Color(0xFF6200EE)
val Purple700 = Color(0xFF3700B3)
val Purple200 = Color(0xFFBB86FC)
val Teal200 = Color(0xFF03DAC5)

// 2. Темная палитра (сопоставляем с цветами из themes-night.xml)
private val DarkColorScheme = darkColorScheme(
    primary = Purple200,
    secondary = Teal200,
    tertiary = Color.White,
    background = Color(0xFF121212),
    surface = Color(0xFF121212),
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

// 3. Светлая палитра (сопоставляем с цветами из themes.xml)
private val LightColorScheme = lightColorScheme(
    primary = Purple500,
    secondary = Teal200,
    tertiary = Purple700,
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = Color(0xFF494949), // ваш text_title
    onSurface = Color(0xFF494949)
)

// 4. Основная функция темы
@Composable
fun WeatherAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Выбираем нужную цветовую схему
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Настройка статус-бара (чтобы он совпадал по цвету с темой)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    // 5. Обертка MaterialTheme, которая делает цвета доступными в UI
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}