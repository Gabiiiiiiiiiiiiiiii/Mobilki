package com.example.prottect_yourself

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.prottect_yourself.databinding.MainKarcasBinding

class MainKarcasActivity : AppCompatActivity() {

    private lateinit var binding: MainKarcasBinding

    private val requestMultiplePermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // Этот код выполнится после того, как пользователь ответит на запрос
            val smsGranted = permissions[Manifest.permission.SEND_SMS] ?: false
            val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false

            if (smsGranted && locationGranted) {
                // Все разрешения получены
                Toast.makeText(this, "Все разрешения предоставлены!", Toast.LENGTH_SHORT).show()
            } else {
                // Какие-то разрешения не были предоставлены
                Toast.makeText(this, "Некоторые разрешения не были предоставлены", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = MainKarcasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Проверяем наличие разрешений
        checkAndRequestPermissions()

        val navView: BottomNavigationView = binding.navView

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        // 1. Создаем конфигурацию. Указываем, какие экраны являются главными.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment, R.id.contactsFragment, R.id.wordsFragment, R.id.emailFragment, R.id.dictaphoneFragment
            )
        )

        // 2. Связываем NavController с ActionBar (верхней панелью), используя нашу конфигурацию.
        setupActionBarWithNavController(navController, appBarConfiguration)

        navView.setupWithNavController(navController)
    }

    // 1. Создаем лаунчер для запроса НЕСКОЛЬКИХ разрешений сраз

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        // Проверяем разрешение на SMS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.SEND_SMS)
        }
        // Проверяем разрешение на геолокацию
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // Если есть что запрашивать, запускаем лаунчер
        if (permissionsToRequest.isNotEmpty()) {
            requestMultiplePermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}