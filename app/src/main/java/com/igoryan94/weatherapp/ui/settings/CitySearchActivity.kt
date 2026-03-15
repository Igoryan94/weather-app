package com.igoryan94.weatherapp.ui.settings

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.igoryan94.weatherapp.WeatherApplication
import com.igoryan94.weatherapp.data.repository.WeatherRepository
import com.igoryan94.weatherapp.databinding.ActivityCitySearchBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class CitySearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCitySearchBinding
    private lateinit var cityAdapter: CityAdapter
    private var searchJob: Job? = null

    // Внедряем зависимость
    @Inject
    lateinit var repository: WeatherRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Внедрение Dagger
        (applicationContext as WeatherApplication).appComponent.inject(this)

        binding = ActivityCitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupSearch()
    }
    
    /**
     * Настраиваем кнопку "Назад" в Toolbar.
     */
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            finish() // Закрываем Activity без возврата результата
        }
    }

    /**
     * Инициализируем RecyclerView и обрабатываем клик по городу.
     */
    private fun setupRecyclerView() {
        cityAdapter = CityAdapter { selectedCity ->
            // При клике формируем Intent с результатом и закрываем экран
            val resultIntent = Intent().apply {
                putExtra(EXTRA_CITY_NAME, selectedCity)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
        binding.rvCities.adapter = cityAdapter
    }

    /**
     * Настраиваем слушатель ввода с задержкой (Debounce).
     */
    private fun setupSearch() {
        binding.etSearchQuery.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                searchJob?.cancel() // Отменяем прошлый поиск

                if (query.length >= 2) {
                    searchJob = lifecycleScope.launch {
                        delay(1500)
                        performSearch(query)
                    }
                } else {
                    cityAdapter.submitList(emptyList())
                    binding.progressBar.isVisible = false
                }
            }
        })
    }

    /**
     * Выполнение сетевого запроса к Репозиторию.
     */
    private suspend fun performSearch(query: String) {
        binding.progressBar.isVisible = true
        try {
            // Запрашиваем города из сети
            val results = repository.searchCities(query)
            cityAdapter.submitList(results)
        } catch (_: Exception) {
            // Ошибка сети (например, 429). В идеале TODO показать Snackbar
            cityAdapter.submitList(emptyList())
        } finally {
            binding.progressBar.isVisible = false
        }
    }

    companion object {
        const val EXTRA_CITY_NAME = "extra_city_name"
    }
}