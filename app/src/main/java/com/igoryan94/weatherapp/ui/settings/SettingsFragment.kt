package com.igoryan94.weatherapp.ui.settings

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.igoryan94.weatherapp.MainActivity
import com.igoryan94.weatherapp.R
import com.igoryan94.weatherapp.WeatherApplication
import com.igoryan94.weatherapp.data.repository.SettingsRepository.Companion.THEME_DARK
import com.igoryan94.weatherapp.data.repository.SettingsRepository.Companion.THEME_LIGHT
import com.igoryan94.weatherapp.data.repository.SettingsRepository.Companion.THEME_SYSTEM
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
    private lateinit var fusedLocationClient: FusedLocationProviderClient

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

    /**
     * Регистратор для запроса разрешений на геолокацию.
     * Вызывается, когда пользователь включает свитч GPS.
     */
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                    permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Разрешение получено. Включаем настройку и запрашиваем координаты.
                settingsViewModel.setGpsState(true)
                fetchLocationAndSave()
            }

            else -> {
                // В разрешении отказано. Откатываем свитч назад.
                Toast.makeText(
                    requireContext(),
                    "Для этой функции нужен доступ к местоположению",
                    Toast.LENGTH_SHORT
                ).show()
                binding.switchLocation.isChecked = false
                settingsViewModel.setGpsState(false)
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

        alarmScheduler = WeatherAlarmScheduler(requireContext())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        observeViewModel()
        setupInteractions()
        setupNotificationSwitch()
        setupTimePicker()

        // Обновляем текст текущего города при запуске
        binding.tvCurrentCity.text = settingsViewModel.getCurrentCity()

        // Запускаем загрузку настроек из БД и SharedPreferences
        settingsViewModel.loadSettings()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupInteractions() {
        // Наблюдаем за текущим городом
        settingsViewModel.selectedCity.observe(viewLifecycleOwner) { city ->
            binding.tvCurrentCity.text = city
        }

        // Слушатели кликов
        binding.switchLocation.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.setGpsState(isChecked)
        }

        binding.btnSearchCity.setOnClickListener {
            val intent = Intent(requireContext(), CitySearchActivity::class.java)
            citySearchLauncher.launch(intent)
        }

        // Установка начального состояния при входе на экран
        val currentUnit = settingsViewModel.getUnits() // "metric" или "imperial"
        if (currentUnit == "metric") {
            binding.toggleUnits.check(R.id.btnMetric)
        } else {
            binding.toggleUnits.check(R.id.btnImperial)
        }

        // Сохранение через правильный слушатель
        binding.toggleUnits.addOnButtonCheckedListener { _, checkedId, isChecked ->
            // Проверяем isChecked, чтобы код не срабатывал дважды
            // (один раз для развыбора старой кнопки, второй — для выбора новой)
            if (isChecked) {
                val unit = if (checkedId == R.id.btnMetric) "metric" else "imperial"
                settingsViewModel.saveUnits(unit)

                // Для отладки
                Log.d("SETTINGS", "Selected unit: $unit")
            }
        }

        // Устанавливаем текущее состояние из памяти БЕЗ анимации
        val savedMode = settingsViewModel.getSavedThemeMode()
        val checkedId = when (savedMode) {
            1 -> R.id.rbLight
            2 -> R.id.rbDark
            else -> R.id.rbSystem
        }
        binding.rgTheme.check(checkedId)

        // Вешаем слушатель на изменения
        binding.rgTheme.setOnCheckedChangeListener { group, id ->
            // Определяем, какой режим выбрал пользователь
            val selectedMode = when (id) {
                R.id.rbLight -> THEME_LIGHT
                R.id.rbDark -> THEME_DARK
                else -> THEME_SYSTEM
            }

            // Сохраняем в настройки
            settingsViewModel.saveThemeMode(selectedMode)

            // Вызываем анимацию в MainActivity
            // Передаём саму RadioGroup (group) как точку начала анимации (центр круга)
            (requireActivity() as MainActivity).changeThemeWithAnimation(selectedMode, group)
        }
    }

    private fun observeViewModel() {
        // Восстановление состояния уведомлений
        settingsViewModel.notificationSettings.observe(viewLifecycleOwner) { entity ->
            if (entity != null) {
                // Отключаем слушатель, чтобы программное изменение не вызвало логику сохранения
                binding.switchDailyNotification.setOnCheckedChangeListener(null)

                binding.switchDailyNotification.isChecked = entity.isEnabled
                binding.llTimePickerContainer.visibility =
                    if (entity.isEnabled) View.VISIBLE else View.GONE
                binding.tvNotificationTime.text =
                    String.format("%02d:%02d", entity.hour, entity.minute)

                // Возвращаем слушатель на место
                setupNotificationSwitch()
            }
        }

        // Наблюдаем за геолокацией: если включена, блокируем ручной выбор
        settingsViewModel.isGpsEnabled.observe(viewLifecycleOwner) { isGpsEnabled ->
            binding.switchLocation.isChecked = isGpsEnabled
            binding.btnSearchCity.isEnabled = !isGpsEnabled
            binding.btnSearchCity.alpha = if (isGpsEnabled) 0.5f else 1.0f
            setupGpsSwitch()
        }
    }

    /**
     * Проверка разрешений перед запуском GPS.
     */
    private fun checkLocationPermissionAndFetch() {
        val fineLocation = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocation = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (fineLocation == PackageManager.PERMISSION_GRANTED || coarseLocation == PackageManager.PERMISSION_GRANTED) {
            settingsViewModel.setGpsState(true)
            fetchLocationAndSave()
        } else {
            // Если разрешений нет, запрашиваем их
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    /**
     * Настройка переключателя GPS.
     */
    private fun setupGpsSwitch() {
        binding.switchLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkLocationPermissionAndFetch()
            } else {
                settingsViewModel.setGpsState(false)
                Toast.makeText(
                    requireContext(),
                    "Автоопределение выключено. Выберите город вручную.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Настройка переключателя уведомлений.
     */
    private fun setupNotificationSwitch() {
        binding.switchDailyNotification.setOnCheckedChangeListener { _, isChecked ->
            binding.llTimePickerContainer.visibility = if (isChecked) View.VISIBLE else View.GONE

            val timeText = binding.tvNotificationTime.text.toString()
            val (hour, minute) = if (timeText.contains(":")) {
                timeText.split(":").map { it.toInt() }
            } else {
                listOf(7, 0) // Дефолтное время 07:00
            }

            // Целевой город берем из настроек, чтобы пуш показывал погоду для актуального места
            val targetCity = settingsViewModel.getCurrentCity()

            if (isChecked) {
                alarmScheduler.scheduleDailyAlarm(hour, minute)
            } else {
                alarmScheduler.cancelAlarm()
            }

            settingsViewModel.saveNotificationSettings(
                hour,
                minute,
                targetCity,
                settingsViewModel.getUnits() == "metric",
                isChecked
            )
        }
    }

    /**
     * Вызов диалога выбора времени и перепланирование уведомления.
     */
    private fun setupTimePicker() {
        binding.llTimePickerContainer.setOnClickListener {
            val timeText = binding.tvNotificationTime.text.toString()
            val (currentHour, currentMinute) = if (timeText.contains(":")) {
                timeText.split(":").map { it.toInt() }
            } else {
                listOf(7, 0)
            }

            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(currentHour)
                .setMinute(currentMinute)
                .setTitleText("Выберите время прогноза")
                .build()

            picker.addOnPositiveButtonClickListener {
                val selectedHour = picker.hour
                val selectedMinute = picker.minute

                @SuppressLint("DefaultLocale")
                binding.tvNotificationTime.text =
                    String.format("%02d:%02d", selectedHour, selectedMinute)

                alarmScheduler.scheduleDailyAlarm(selectedHour, selectedMinute)

                val targetCity = settingsViewModel.getCurrentCity()
                settingsViewModel.saveNotificationSettings(
                    selectedHour,
                    selectedMinute,
                    targetCity,
                    settingsViewModel.getUnits() == "metric",
                    isEnabled = true
                )
            }

            picker.show(childFragmentManager, "time_picker")
        }
    }

    /**
     * Получение последних известных координат устройства и сохранение их как "города".
     */
    @SuppressLint("MissingPermission") // Подавляем ошибку, так как проверка разрешений выполняется выше
    private fun fetchLocationAndSave() {
        Toast.makeText(requireContext(), "Определение местоположения...", Toast.LENGTH_SHORT).show()

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                // Формируем строку вида "55.75,37.62" - WeatherAPI отлично её понимает
                val coordinatesString = "${location.latitude},${location.longitude}"

                // Сохраняем в SharedPreferences
                settingsViewModel.saveCity(coordinatesString)

                // Обновляем UI
                binding.tvCurrentCity.text = "Текущая геопозиция"
            } else {
                Toast.makeText(
                    requireContext(),
                    "Не удалось получить GPS. Включите геолокацию на устройстве.",
                    Toast.LENGTH_LONG
                ).show()
                binding.switchLocation.isChecked = false
                settingsViewModel.setGpsState(false)
            }
        }
    }
}