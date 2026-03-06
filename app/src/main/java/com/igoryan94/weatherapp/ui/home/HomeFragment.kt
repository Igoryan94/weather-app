package com.igoryan94.weatherapp.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.igoryan94.weatherapp.WeatherApplication
import com.igoryan94.weatherapp.databinding.FragmentHomeBinding
import javax.inject.Inject

class HomeFragment : Fragment() {

    @Inject
    lateinit var factory: HomeViewModelFactory

    private lateinit var homeViewModel: HomeViewModel

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

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

        // Получаем ViewModel через фабрику Dagger, если она настроена
        val homeViewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        // Подписываемся на изменения состояния погоды
        homeViewModel.weatherState.observe(viewLifecycleOwner) { state ->
            Log.d("WEATHER_DEBUG", "UI: Получены новые данные для ${state.location}")

            with(binding) {
                tvLocation.text = state.location
                tvCurrentTemp.text = state.currentTemp
                tvTempRange.text = state.tempRange
                tvHumidity.text = state.humidity
                tvWindSpeed.text = state.windSpeed
                // Заполняем краткий статус из первого элемента списка
                tvState.text = state.forecastDays.getOrNull(0) ?: ""
            }
        }

        // Выполняем первый запрос для Москвы
        homeViewModel.fetchCurrentWeather("Moscow")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}