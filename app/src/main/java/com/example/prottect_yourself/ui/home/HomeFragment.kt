package com.example.prottect_yourself.ui.home

import android.Manifest
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
import com.example.prottect_yourself.databinding.FragmentHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Клиент для работы с геолокацией
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Лаунчер для запроса разрешения на доступ к геолокации
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Разрешение получено, можно снова попытаться получить координаты
                Toast.makeText(requireContext(), "Разрешение получено, нажмите еще раз", Toast.LENGTH_LONG).show()
            } else {
                // Пользователь отказал в разрешении
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

        // 1. Инициализируем FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // 2. Устанавливаем обработчик нажатия на кнопку
        binding.mainStartButton.setOnClickListener {
            checkPermissionAndGetLocation()
        }
    }

    private fun checkPermissionAndGetLocation() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION // Запрашиваем точные координаты
            ) == PackageManager.PERMISSION_GRANTED -> {
                // 3. Если разрешение уже есть, получаем координаты
                getCurrentLocation()
            }
            else -> {
                // 4. Если разрешения нет, запрашиваем его у пользователя
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun getCurrentLocation() {
        // Проверка разрешения здесь нужна формально, чтобы Android Studio не ругалась.
        // Мы уже проверили его в checkPermissionAndGetLocation().
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        // 5. Запрашиваем текущее местоположение с высоким приоритетом точности
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude

                    // 6. Выводим координаты в Logcat
                    Log.d("GPS_COORDINATES", "Широта: $latitude, Долгота: $longitude")
                    Toast.makeText(requireContext(), "Координаты получены! См. Logcat (d)", Toast.LENGTH_LONG).show()
                } else {
                    Log.d("GPS_COORDINATES", "Не удалось получить местоположение.")
                    Toast.makeText(requireContext(), "Не удалось получить местоположение", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("GPS_COORDINATES", "Ошибка при получении местоположения", e)
                Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}