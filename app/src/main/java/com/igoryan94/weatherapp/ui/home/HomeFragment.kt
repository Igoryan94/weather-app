package com.igoryan94.weatherapp.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.igoryan94.weatherapp.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получаем ViewModel через фабрику Dagger, если она настроена
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        // Подписываемся на изменения состояния погоды
        homeViewModel.weatherState.observe(viewLifecycleOwner) { state ->
            with(binding) {
                tvLocation.text = state.location
                tvCurrentTemp.text = state.currentTemp
                tvTempRange.text = state.tempRange
                tvHumidity.text = state.humidity
                tvWindSpeed.text = state.windSpeed
                // Заполняем краткий статус из первого элемента списка
                tvDay1.text = state.forecastDays[0]
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