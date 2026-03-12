package com.igoryan94.weatherapp.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
}