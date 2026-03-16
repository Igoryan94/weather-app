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
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.igoryan94.weatherapp.WeatherApplication
import com.igoryan94.weatherapp.data.repository.WeatherRepository
import com.igoryan94.weatherapp.databinding.FragmentSettingsBinding
import com.igoryan94.weatherapp.notifications.WeatherAlarmScheduler
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

    private lateinit var alarmScheduler: WeatherAlarmScheduler

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

        alarmScheduler = WeatherAlarmScheduler(requireContext())

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

        setupNotificationSwitch()
        setupTimePicker()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Настройка переключателя (Switch) включения/выключения уведомлений.
     */
    private fun setupNotificationSwitch() {
        binding.switchDailyNotification.setOnCheckedChangeListener { _, isChecked ->
            binding.llTimePickerContainer.visibility = if (isChecked) View.VISIBLE else View.GONE

            if (!isChecked) {
                // Если выключили - отменяем будильник
                alarmScheduler.cancelAlarm()

                // TODO: Здесь нужно вызвать ViewModel для сохранения в БД (NotificationDao) isEnabled = false
            } else {
                // Если включили - ставим будильник на текущее время в TextView (например, 07:00)
                val timeText = binding.tvNotificationTime.text.toString()
                val (hour, minute) = timeText.split(":").map { it.toInt() }

                alarmScheduler.scheduleDailyAlarm(hour, minute)

                // TODO: Вызвать ViewModel для сохранения в БД (NotificationEntity(hour=hour, minute=minute, isEnabled=true...))
            }
        }
    }

    /**
     * Настройка вызова окна выбора времени.
     */
    private fun setupTimePicker() {
        binding.llTimePickerContainer.setOnClickListener {
            // Создаем MaterialTimePicker в 24-часовом формате
            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(7)
                .setMinute(0)
                .setTitleText("Выберите время прогноза")
                .build()

            // Слушатель нажатия "ОК" в диалоге
            picker.addOnPositiveButtonClickListener {
                val selectedHour = picker.hour
                val selectedMinute = picker.minute

                // Форматируем для TextView (добавляем нули, чтобы было 07:05, а не 7:5)
                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                binding.tvNotificationTime.text = formattedTime

                // Перепланируем будильник на новое время
                alarmScheduler.scheduleDailyAlarm(selectedHour, selectedMinute)

                // TODO: Сохранить новые часы и минуты в БД через ViewModel
            }

            picker.show(childFragmentManager, "time_picker")
        }
    }
}