package com.igoryan94.weatherapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.graphics.createBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.transition.TransitionInflater
import com.igoryan94.weatherapp.WeatherApplication
import com.igoryan94.weatherapp.ui.forecast.ForecastDayUiModel
import com.igoryan94.weatherapp.ui.theme.WeatherAppTheme
import javax.inject.Inject

class HomeFragment : Fragment() {

    @Inject
    lateinit var factory: HomeViewModelFactory

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Настраиваем анимацию Shared Element
        sharedElementEnterTransition = TransitionInflater.from(requireContext())
            .inflateTransition(android.R.transition.move)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Внедряем зависимости до создания View
        (requireActivity().application as WeatherApplication).appComponent.inject(this)
        homeViewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        // Инициализируем ComposeView
        return ComposeView(requireContext()).apply {
            // Устанавливаем стратегию уничтожения Compose-композиции вместе с View
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            // Восстанавливаем имя для Shared Element Transition, чтобы анимация из списка работала
            transitionName = "home_shared_element_target"

            setContent {
                WeatherAppTheme {
                    val passedForecast =
                        arguments?.getParcelable<ForecastDayUiModel>("selected_forecast")

                    if (passedForecast != null) {
                        // Режим "Выбран день из прогноза"
                        val forecastState = HomeWeatherState(
                            location = "Прогноз на: ${passedForecast.date}",
                            currentTemp = passedForecast.tempDay,
                            feelsLike = passedForecast.feelsLike,
                            humidity = "—",
                            windSpeed = "—",
                            forecastDays = listOf(passedForecast.condition)
                        )

                        HomeScreen(
                            state = forecastState,
                            onShareClick = { shareScreenshot(getBitmapFromView(this@apply)) }
                        )
                    } else {
                        // Режим "Обычный запуск". Подписываемся на LiveData как на State
                        val state by homeViewModel.weatherState.observeAsState(
                            HomeWeatherState(
                                location = "Загрузка..."
                            )
                        )

                        HomeScreen(
                            state = state,
                            onShareClick = { shareScreenshot(getBitmapFromView(this@apply)) }
                        )
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Логика запроса данных (если это обычный запуск)
        val passedForecast = arguments?.getParcelable<ForecastDayUiModel>("selected_forecast")
        if (passedForecast == null) {
            if (homeViewModel.weatherState.value == null || homeViewModel.weatherState.value?.location.isNullOrBlank()) {
                homeViewModel.loadWeatherForSavedCity()
            }
        }
    }

    /**
     * Функция для создания Bitmap (изображения) из переданной View.
     */
    private fun getBitmapFromView(view: View): android.graphics.Bitmap {
        val bitmap = createBitmap(view.width, view.height)
        val canvas = android.graphics.Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    /**
     * Функция для сохранения Bitmap в кэш и вызова системного диалога "Поделиться".
     */
    private fun shareScreenshot(bitmap: android.graphics.Bitmap) {
        try {
            val cachePath = java.io.File(requireContext().cacheDir, "images")
            cachePath.mkdirs()
            val file = java.io.File(cachePath, "weather_screenshot.png")
            val fileOutputStream = java.io.FileOutputStream(file)

            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()

            val contentUri = androidx.core.content.FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )

            if (contentUri != null) {
                val shareIntent = android.content.Intent().apply {
                    action = android.content.Intent.ACTION_SEND
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    setDataAndType(contentUri, requireContext().contentResolver.getType(contentUri))
                    putExtra(android.content.Intent.EXTRA_STREAM, contentUri)
                    type = "image/png"
                }
                startActivity(
                    android.content.Intent.createChooser(
                        shareIntent,
                        "Поделиться погодой"
                    )
                )
            }
        } catch (e: java.io.IOException) {
            e.printStackTrace()
        }
    }
}