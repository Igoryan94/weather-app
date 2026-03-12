package com.igoryan94.weatherapp.ui.forecast

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.igoryan94.weatherapp.WeatherApplication
import com.igoryan94.weatherapp.databinding.FragmentForecastBinding
import javax.inject.Inject

class ForecastFragment : Fragment() {

    @Inject
    lateinit var factory: ForecastViewModelFactory

    private lateinit var forecastViewModel: ForecastViewModel

    private var _binding: FragmentForecastBinding? = null
    private val binding get() = _binding!!

    // Объявляем адаптер
    private lateinit var forecastAdapter: ForecastAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForecastBinding.inflate(inflater, container, false)
        val root: View = binding.root

        (requireActivity().application as WeatherApplication).appComponent.inject(this)

        forecastViewModel = ViewModelProvider(this, factory)[ForecastViewModel::class.java]

        // Инициализация RecyclerView
        setupRecyclerView()

        // Подписка на общее состояние
        forecastViewModel.state.observe(viewLifecycleOwner) { state ->
            // Когда данные меняются, передаём их в дальнейшую обработку на UI
            renderState(state)
        }

        // Запрос на загрузку данных (пока тестовых)
        forecastViewModel.loadForecastData()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Метод для настройки RecyclerView: установка LayoutManager и привязка адаптера
    private fun setupRecyclerView() {
        forecastAdapter = ForecastAdapter() // Создаем пустой адаптер
        binding.rvForecast.apply {
            // LinearLayoutManager располагает элементы вертикальным списком (уже задано в XML, но дублируем для надежности)
            layoutManager = LinearLayoutManager(context)
            // Привязываем наш адаптер к UI-элементу
            adapter = forecastAdapter
        }
    }

    private fun renderState(state: ForecastState) {
        when (state) {
            is ForecastState.Loading -> {
                binding.progressBar.isVisible = true
                binding.ivError.isVisible = false
                binding.rvForecast.isVisible = false
            }

            is ForecastState.Success -> {
                binding.progressBar.isVisible = false
                binding.ivError.isVisible = false
                binding.rvForecast.isVisible = true
                forecastAdapter.updateData(state.data)
            }

            is ForecastState.Error -> {
                binding.progressBar.isVisible = false
                binding.ivError.isVisible = true
                binding.rvForecast.isVisible = false
            }
        }
    }
}