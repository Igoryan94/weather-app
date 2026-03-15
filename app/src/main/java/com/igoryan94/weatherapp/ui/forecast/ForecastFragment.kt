package com.igoryan94.weatherapp.ui.forecast

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.igoryan94.weatherapp.R
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity().application as WeatherApplication).appComponent.inject(this)
        forecastViewModel = ViewModelProvider(this, factory)[ForecastViewModel::class.java]

        // Инициализация RecyclerView
        setupRecyclerView()

        // Подписка на общее состояние
        forecastViewModel.state.observe(viewLifecycleOwner) { state ->
            // Когда данные меняются, передаём их в дальнейшую обработку на UI
            renderState(state)
        }

        // ВАЖНО: Проверяем, есть ли уже загруженные данные.
        // Если state пуст (первый запуск), то грузим из сети.
        // Если мы вернулись по кнопке "Назад", данные уже будут во ViewModel, и сеть дергаться не будет.
        if (forecastViewModel.state.value == null) {
            forecastViewModel.loadForecastData()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Метод для настройки RecyclerView: установка LayoutManager и привязка адаптера
    private fun setupRecyclerView() {
        // Передаем обработчик клика в адаптер
        forecastAdapter = ForecastAdapter { sharedView, forecastModel ->
            // Подготавливаем данные для передачи
            val bundle = bundleOf("selected_forecast" to forecastModel)

            // Связываем View из списка с будущей View на главном экране
            val extras = FragmentNavigatorExtras(
                sharedView to "home_shared_element_target"
            )

            // Выполняем переход
            findNavController().navigate(
                R.id.action_forecastFragment_to_homeFragment,
                bundle,
                null,
                extras
            )
        }

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