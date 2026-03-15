package com.igoryan94.weatherapp.ui.settings

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.igoryan94.weatherapp.WeatherApplication
import com.igoryan94.weatherapp.data.repository.WeatherRepository
import com.igoryan94.weatherapp.databinding.FragmentSettingsBinding
import javax.inject.Inject

class SettingsFragment : Fragment() {

    // Внедряем репозиторий через Dagger
    @Inject
    lateinit var repository: WeatherRepository

    @Inject
    lateinit var factory: SettingsViewModelFactory

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var settingsViewModel: SettingsViewModel

    private val citySearchLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedCity = result.data?.getStringExtra(CitySearchActivity.EXTRA_CITY_NAME)
            selectedCity?.let {
                // Обновляем UI и сохраняем выбор города
                binding.tvCurrentCity.text = it
                settingsViewModel.setSelectedCity(it)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Внедряем зависимости
        (requireActivity().application as WeatherApplication).appComponent.inject(this)

        settingsViewModel = ViewModelProvider(this, factory)[SettingsViewModel::class.java]

        // Наблюдаем за геолокацией: если включена, блокируем ручной выбор
        settingsViewModel.useLocation.observe(viewLifecycleOwner) { isGpsEnabled ->
            binding.switchLocation.isChecked = isGpsEnabled
            binding.btnSearchCity.isEnabled = !isGpsEnabled
            binding.btnSearchCity.alpha = if (isGpsEnabled) 0.5f else 1.0f
        }

        // Наблюдаем за текущим городом
        settingsViewModel.selectedCity.observe(viewLifecycleOwner) { city ->
            binding.tvCurrentCity.text = city
        }

        // Слушатели кликов
        binding.switchLocation.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.setUseLocation(isChecked)
        }

        binding.btnSearchCity.setOnClickListener {
            val intent = Intent(requireContext(), CitySearchActivity::class.java)
            citySearchLauncher.launch(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}