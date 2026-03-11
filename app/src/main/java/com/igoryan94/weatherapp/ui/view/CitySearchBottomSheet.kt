package com.igoryan94.weatherapp.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.igoryan94.weatherapp.data.repository.WeatherRepository
import com.igoryan94.weatherapp.databinding.BottomSheetCitySearchBinding
import kotlinx.coroutines.launch
import kotlin.math.ceil

class CitySearchBottomSheet(
    private val repository: WeatherRepository,
    private val onCitySelected: (String) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetCitySearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var cityAdapter: CityAdapter

    // Переменные для искусственной пагинации
    private var fullResultsList = listOf<String>()
    private var currentlyLoadedPage = 1
    private val pageSize = 15
    private var totalPages = 0
    private var needLoading = true

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
        initRecyclerView()
        initSearchInput()
    }

    // Инициализация списка и слушателя скролла
    private fun initRecyclerView() {
        cityAdapter = CityAdapter { city ->
            onCitySelected(city)
            dismiss()
        }

        binding.rvCities.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cityAdapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy > 0) { // Скролл вниз
                        val visibleItemCount = layoutManager!!.childCount
                        val totalItemCount = layoutManager!!.itemCount
                        val pastVisibleItemCount =
                            (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()

                        // Вызов метода пагинации
                        doSearchPagination(
                            visibleItemCount,
                            totalItemCount,
                            pastVisibleItemCount,
                            binding.etSearchQuery.text.toString()
                        )
                    }
                }
            })
        }
    }

    // Метод пагинации
    private fun doSearchPagination(
        visibleItemCount: Int,
        totalItemCount: Int,
        pastVisibleItemCount: Int,
        query: String
    ) {
        if (needLoading) {
            // Если дошли почти до конца списка (осталось 5 элементов)
            if ((visibleItemCount + pastVisibleItemCount) >= totalItemCount - 5) {
                needLoading = false

                val pageToLoad = currentlyLoadedPage + 1
                if (pageToLoad > totalPages) return

                binding.progressBar.isVisible = true

                // Имитируем сетевую задержку для наглядности пагинации
                binding.rvCities.postDelayed({
                    loadPageFromCache(pageToLoad)
                }, 500)
            }
        }
    }

    // Логика первичного поиска и разбивки
    private fun initSearchInput() {
        binding.etSearchQuery.addTextChangedListener { text ->
            val query = text.toString().trim()
            if (query.length >= 2) {
                performInitialSearch(query)
            }
        }
    }

    private fun performInitialSearch(query: String) {
        lifecycleScope.launch {
            val currentBinding = _binding ?: return@launch

            currentBinding.progressBar.isVisible = true

            try {// Получаем ВЕСЬ список от API
                val rawResults = repository.searchCities(query)
                fullResultsList = rawResults

                // Рассчитываем количество "искусственных" страниц
                totalPages = ceil(fullResultsList.size.toDouble() / pageSize).toInt()
                currentlyLoadedPage = 1
                needLoading = true

                // Отображаем первую порцию (15 штук)
                val firstPage = fullResultsList.take(pageSize)
                cityAdapter.submitList(firstPage)
            } catch (_: Exception) {
            } finally {
                _binding?.progressBar?.isVisible = false
            }
        }
    }

    // Функция "догрузки" страницы из локального списка
    private fun loadPageFromCache(page: Int) {
        val startIdx = (page - 1) * pageSize
        val endIdx = (startIdx + pageSize).coerceAtMost(fullResultsList.size)

        if (startIdx < fullResultsList.size) {
            val nextPageItems = fullResultsList.subList(startIdx, endIdx)
            cityAdapter.addItems(nextPageItems)

            currentlyLoadedPage = page
            needLoading = true // Снова разрешаем загрузку следующей страницы
        }

        binding.progressBar.isVisible = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}