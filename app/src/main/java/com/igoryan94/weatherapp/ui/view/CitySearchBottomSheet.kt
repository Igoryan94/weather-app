package com.igoryan94.weatherapp.ui.settings

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.igoryan94.weatherapp.data.repository.WeatherRepository
import com.igoryan94.weatherapp.databinding.BottomSheetCitySearchBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CitySearchBottomSheet(
    private val repository: WeatherRepository,
    private val onCitySelected: (String) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetCitySearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var cityAdapter: CityAdapter
    private var searchJob: Job? = null // Переменная для управления жизненным циклом поиска

    // Переменные для искусственной пагинации
    private var fullResultsList = listOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCitySearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
    }

    private fun performSearch(query: String) {
        lifecycleScope.launch {
            val currentBinding = _binding ?: return@launch

            currentBinding.progressBar.isVisible = true

            try {
                fullResultsList = repository.searchCities(query)
                cityAdapter.submitList(fullResultsList)
            } catch (_: Exception) {
            } finally {
                _binding?.progressBar?.isVisible = false
            }
        }
    }

    // Инициализация списка
    private fun setupRecyclerView() {
        cityAdapter = CityAdapter { city ->
            onCitySelected(city)
            dismiss()
        }
        binding.rvCities.adapter = cityAdapter
    }

    private fun setupSearch() {
        binding.etSearchQuery.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()

                // Отменяем предыдущий запланированный поиск, если пользователь нажал новую букву
                searchJob?.cancel()

                if (query.length >= 4) {
                    // Запускаем новую корутину с задержкой
                    searchJob = viewLifecycleOwner.lifecycleScope.launch {
                        delay(3000)

                        // Если за время корутина не была отменена новым вводом - ищем
                        performSearch(query)
                    }
                } else {
                    // Если запрос слишком короткий, очищаем список
                    cityAdapter.submitList(emptyList())
                    binding.progressBar.isVisible = false
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}