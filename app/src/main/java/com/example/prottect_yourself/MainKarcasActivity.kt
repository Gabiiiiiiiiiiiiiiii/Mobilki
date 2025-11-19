package com.example.prottect_yourself

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.prottect_yourself.databinding.MainKarcasBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainKarcasActivity : AppCompatActivity() {

    private lateinit var binding: MainKarcasBinding
    private var speechHelper: SpeechRecognitionHelper? = null

    // Новый способ запроса разрешений
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Если пользователь дал разрешение, запускаем распознавание
                startSpeechRecognition()
            } else {
                // Иначе, показываем сообщение
                Toast.makeText(this, "Разрешение на микрофон необходимо для распознавания речи", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainKarcasBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupNavigation()

        // Проверяем разрешение на микрофон и запускаем распознавание
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED -> {
                // Если разрешение уже есть, запускаем
                startSpeechRecognition()
            }
            else -> {
                // Если разрешения нет, запрашиваем его
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    // Функция для запуска распознавания
    private fun startSpeechRecognition() {
        // Мы больше не создаем и не передаем WordStorage
        speechHelper = SpeechRecognitionHelper(this) { detectedKeyword ->
            // Когда слово распознано, выводим его в консоль
            Log.d("SpeechHelper", "УСПЕХ! Распознано кодовое слово: '$detectedKeyword'")
            Toast.makeText(this, "Распознано: $detectedKeyword", Toast.LENGTH_SHORT).show()
        }
        speechHelper?.startListening()
    }

    private fun setupNavigation() {
        val navView: BottomNavigationView = binding.navView
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment, R.id.contactsFragment, R.id.wordsFragment, R.id.emailFragment, R.id.dictaphoneFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onDestroy() {
        super.onDestroy()
        speechHelper?.destroy()
    }
}
