package com.example.prottect_yourself.ui.home

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.prottect_yourself.databinding.FragmentHomeBinding
import com.example.prottect_yourself.logic.services.VoiceBackgroundService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Клиент для работы с геолокацией
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // --- 1. ЛАУНЧЕРЫ РАЗРЕШЕНИЙ (Цепочка обратных вызовов) ---

    // Шаг 1: Лаунчер для МИКРОФОНА
    private val audioPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Микрофон дали -> переходим к проверке SMS
                checkSmsPermissionAndStart()
            } else {
                Toast.makeText(requireContext(), "Нужен доступ к микрофону!", Toast.LENGTH_SHORT).show()
            }
        }

    // Шаг 2: Лаунчер для SMS (НОВЫЙ)
    private val smsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // SMS дали -> переходим к проверке Геолокации
                checkLocationPermissionAndStart()
            } else {
                Toast.makeText(requireContext(), "Нужен доступ к отправке SMS!", Toast.LENGTH_SHORT).show()
            }
        }

    // Шаг 3: Лаунчер для ГЕОЛОКАЦИИ
    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Гео дали -> ЗАПУСКАЕМ ВСЁ
                performStartActions()
            } else {
                Toast.makeText(requireContext(), "Доступ к геолокации необходим", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализируем FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Проверяем состояние сервиса при запуске
        updateUIState(isServiceRunning(VoiceBackgroundService::class.java))

        // Обработчик нажатия на кнопку
        binding.mainStartButton.setOnClickListener {
            if (isServiceRunning(VoiceBackgroundService::class.java)) {
                // Если работает -> ОСТАНАВЛИВАЕМ
                stopVoiceService()
            } else {
                // Если не работает -> ЗАПУСКАЕМ (начинаем с проверки Аудио)
                checkAudioPermissionAndStart()
            }
        }
    }

    // --- 2. ФУНКЦИИ ПРОВЕРКИ (Цепочка вызовов) ---

    // Шаг 1: Проверка Аудио
    private fun checkAudioPermissionAndStart() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED -> {
                // Аудио есть -> Идем к SMS
                checkSmsPermissionAndStart()
            }
            else -> {
                // Аудио нет -> Запрашиваем
                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    // Шаг 2: Проверка SMS (НОВАЯ ФУНКЦИЯ)
    private fun checkSmsPermissionAndStart() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED -> {
                // SMS есть -> Идем к Геолокации
                checkLocationPermissionAndStart()
            }
            else -> {
                // SMS нет -> Запрашиваем
                smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
            }
        }
    }

    // Шаг 3: Проверка Геолокации
    private fun checkLocationPermissionAndStart() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                // Гео есть -> ЗАПУСК
                performStartActions()
            }
            else -> {
                // Гео нет -> Запрашиваем
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    // Финал: Запуск сервиса и получение координат
    private fun performStartActions() {
        startVoiceService()
        getCurrentLocation()
    }

    // --- УПРАВЛЕНИЕ СЕРВИСОМ И UI ---

    private fun startVoiceService() {
        val intent = Intent(requireContext(), VoiceBackgroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(intent)
        } else {
            requireContext().startService(intent)
        }
        updateUIState(true)
        Toast.makeText(requireContext(), "Защита включена", Toast.LENGTH_SHORT).show()
    }

    private fun stopVoiceService() {
        val intent = Intent(requireContext(), VoiceBackgroundService::class.java)
        requireContext().stopService(intent)
        updateUIState(false)
        Toast.makeText(requireContext(), "Защита выключена", Toast.LENGTH_SHORT).show()
    }

    private fun updateUIState(isRunning: Boolean) {
        if (isRunning) {
            binding.mainStartButton.text = "Выключить"
            binding.mainStartButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FF5252"))
        } else {
            binding.mainStartButton.text = "Включить"
            binding.mainStartButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50"))
        }
    }

    // --- ЛОГИКА GPS ---

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val googleMapsLink = "http://googleusercontent.com/maps.google.com/maps?q=$latitude,$longitude"
                    Log.d("GPS_COORDINATES", "Широта: $latitude, Долгота: $longitude. $googleMapsLink")
                } else {
                    Log.d("GPS_COORDINATES", "Не удалось получить местоположение.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("GPS_COORDINATES", "Ошибка при получении местоположения", e)
            }
    }

    // --- ПРОВЕРКА СТАТУСА СЕРВИСА ---
    @Suppress("DEPRECATION")
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = requireContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}