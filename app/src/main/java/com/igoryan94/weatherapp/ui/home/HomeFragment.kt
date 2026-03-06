package com.igoryan94.weatherapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.igoryan94.weatherapp.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Подписка на обновление состояния
        homeViewModel.weatherState.observe(viewLifecycleOwner) { state ->
            with(binding) {
                tvLocation.text = state.location
                tvCurrentTemp.text = state.currentTemp
                tvTempRange.text = state.tempRange
                tvHumidity.text = state.humidity
                tvWindSpeed.text = state.windSpeed

                // Простое заполнение строк прогноза
                if (state.forecastDays.size >= 3) {
                    tvDay1.text = state.forecastDays[0]
                    tvDay2.text = state.forecastDays[1]
                    tvDay3.text = state.forecastDays[2]
                }
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}