package com.igoryan94.weatherapp.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.createBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.transition.TransitionInflater
import com.igoryan94.weatherapp.WeatherApplication
import com.igoryan94.weatherapp.databinding.FragmentHomeBinding
import com.igoryan94.weatherapp.ui.forecast.ForecastDayUiModel
import javax.inject.Inject

class HomeFragment : Fragment() {

    @Inject
    lateinit var factory: HomeViewModelFactory

    private lateinit var homeViewModel: HomeViewModel

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Настраиваем анимацию Shared Element
        sharedElementEnterTransition = TransitionInflater.from(requireContext())
            .inflateTransition(android.R.transition.move)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Внедряем зависимости
        (requireActivity().application as WeatherApplication).appComponent.inject(this)

        // Получаем ViewModel через фабрику Dagger
        homeViewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        // Подписываемся на обновления стандартного состояния (если грузим из сети)
        homeViewModel.weatherState.observe(viewLifecycleOwner) { state ->
            Log.d("WEATHER_DEBUG", "UI: Получены новые данные для ${state.location}")
            populateUi(
                location = state.location,
                currentTemp = state.currentTemp,
                tempRange = state.tempRange,
                humidity = state.humidity,
                windSpeed = state.windSpeed,
                condition = state.forecastDays.getOrNull(0) ?: ""
            )
        }

        binding.btnShare.setOnClickListener {
            // Мы передаем binding.root, чтобы сделать скриншот всего экрана.
            // Или можно передать конкретный CardView с погодой.
            val screenshot = getBitmapFromView(binding.root)
            shareScreenshot(screenshot)
        }

        // Проверяем, пришли ли мы сюда из списка прогнозов
        val passedForecast =
            arguments?.getParcelable("selected_forecast", ForecastDayUiModel::class.java)

        if (passedForecast != null) {
            // Режим "Выбран день из прогноза"
            // ВАЖНО: Мы НЕ вызываем homeViewModel.loadWeatherForSavedCity()

            // Формируем UI на основе переданных данных
            populateUi(
                location = "Прогноз на: ${passedForecast.date}", // Показываем выбранную дату
                currentTemp = passedForecast.tempDay,
                tempRange = passedForecast.tempNight, // В качестве диапазона выводим ночную
                humidity = "—", // В API прогноза влажность может быть в другом поле, пока ставим прочерк
                windSpeed = "—",
                condition = passedForecast.condition
            )
        } else {
            // Режим "Обычный запуск"
            // Если ничего не передали, значит это обычный запуск главной вкладки
            // Проверяем, есть ли уже загруженные данные, чтобы не грузить заново при повороте экрана
            if (homeViewModel.weatherState.value == null || homeViewModel.weatherState.value?.location.isNullOrBlank()) {
                homeViewModel.loadWeatherForSavedCity()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Вспомогательная функция, чтобы не дублировать код заполнения полей
    private fun populateUi(
        location: String,
        currentTemp: String,
        tempRange: String,
        humidity: String,
        windSpeed: String,
        condition: String
    ) {
        with(binding) {
            tvLocation.text = location
            tvCurrentTemp.text = currentTemp
            tvTempRange.text = tempRange
            tvHumidity.text = humidity
            tvWindSpeed.text = windSpeed
            tvState.text = condition
        }
    }

    /**
     * Функция для создания Bitmap (изображения) из переданной View.
     * @param view Корневой элемент макета, который нужно "сфотографировать".
     * @return Сформированное изображение в формате Bitmap.
     */
    private fun getBitmapFromView(view: View): android.graphics.Bitmap {
        // Создаем пустой Bitmap с размерами нашей View
        val bitmap = createBitmap(view.width, view.height)
        // Создаем Canvas (холст), привязанный к этому Bitmap
        val canvas = android.graphics.Canvas(bitmap)
        // Рисуем текущее состояние View на этот холст
        view.draw(canvas)
        return bitmap
    }

    /**
     * Функция для сохранения Bitmap в кэш и вызова системного диалога "Поделиться".
     * @param bitmap Изображение для отправки.
     */
    private fun shareScreenshot(bitmap: android.graphics.Bitmap) {
        try {
            // Подготовка папки в кэше приложения
            val cachePath = java.io.File(requireContext().cacheDir, "images")
            cachePath.mkdirs() // Создаем папку, если её нет

            // Создание файла
            val file = java.io.File(cachePath, "weather_screenshot.png")
            val fileOutputStream = java.io.FileOutputStream(file)

            // Сжатие Bitmap в файл (формат PNG, качество 100%)
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()

            // Получение безопасного URI через наш FileProvider
            val contentUri = androidx.core.content.FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )

            // Формирование Intent для отправки
            if (contentUri != null) {
                val shareIntent = android.content.Intent().apply {
                    action = android.content.Intent.ACTION_SEND
                    // Добавляем флаг доступа на чтение для принимающего приложения
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    setDataAndType(contentUri, requireContext().contentResolver.getType(contentUri))
                    putExtra(android.content.Intent.EXTRA_STREAM, contentUri)
                    type = "image/png"
                }
                // Запуск окна выбора приложений
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