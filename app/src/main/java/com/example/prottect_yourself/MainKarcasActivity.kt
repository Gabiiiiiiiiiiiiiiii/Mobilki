package com.example.prottect_yourself

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.prottect_yourself.databinding.MainKarcasBinding
import com.example.prottect_yourself.logic.services.VoiceBackgroundService
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainKarcasActivity : AppCompatActivity() {

    private lateinit var binding: MainKarcasBinding

    // Мы убрали speechHelper отсюда, так как теперь он живет внутри VoiceBackgroundService

    // Лаунчер разрешения на микрофон
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                startVoiceService()
            } else {
                Toast.makeText(this, "Разрешение на микрофон необходимо для защиты", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainKarcasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()

        // Проверяем разрешение и запускаем сервис при старте приложения
        // (Если вы хотите автозапуск. Если нет - этот блок можно убрать на кнопку "Включить" во фрагменте)
        //checkPermissionsAndStart()
    }

    private fun checkPermissionsAndStart() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED -> {
                startVoiceService()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun startVoiceService() {
        // ИСПРАВЛЕНО: используем 'this' вместо 'requireContext()'
        val intent = Intent(this, VoiceBackgroundService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent) // Метод Activity
        } else {
            startService(intent) // Метод Activity
        }
    }

    // Этот метод можно вызывать, если нужно остановить сервис (например, по кнопке выхода)
    private fun stopVoiceService() {
        // ИСПРАВЛЕНО: используем 'this' вместо 'requireContext()'
        val intent = Intent(this, VoiceBackgroundService::class.java)
        stopService(intent)
    }

    private fun setupNavigation() {
        val navView: BottomNavigationView = binding.navView
        // Получаем NavHostFragment безопасным способом
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as? NavHostFragment
            ?: return // Если фрагмент не найден, выходим (защита от краша)

        val navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.contactsFragment,
                R.id.wordsFragment,
                R.id.emailFragment,
                R.id.dictaphoneFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }
}