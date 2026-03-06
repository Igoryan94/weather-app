package com.igoryan94.weatherapp.ui.forecast

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.igoryan94.weatherapp.databinding.FragmentForecastBinding

class ForecastFragment : Fragment() {

    private var _binding: FragmentForecastBinding? = null
    private val binding get() = _binding!!

    // Объявляем адаптер
    private lateinit var forecastAdapter: ForecastAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val forecastViewModel = ViewModelProvider(this).get(ForecastViewModel::class.java)

        _binding = FragmentForecastBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 1. Инициализация RecyclerView
        setupRecyclerView()

        // 2. Подписка на данные из ViewModel
        forecastViewModel.forecastList.observe(viewLifecycleOwner) { newList ->
            // Когда данные меняются, передаем их в адаптер
            forecastAdapter.updateData(newList)
        }

        // 3. Запрос на загрузку данных (пока тестовых)
        forecastViewModel.loadMockData()

        return root
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}