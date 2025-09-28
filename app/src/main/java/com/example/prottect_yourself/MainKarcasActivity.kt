package com.example.prottect_yourself

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.prottect_yourself.databinding.MainKarcasBinding

class MainKarcasActivity : AppCompatActivity() {

    private lateinit var binding: MainKarcasBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = MainKarcasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        // --- НАЧАЛО ИСПРАВЛЕНИЯ ---

        // 1. Создаем конфигурацию. Указываем, какие экраны являются главными.
        // На них не будет стрелки "назад".
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment, R.id.contactsFragment, R.id.wordsFragment, R.id.emailFragment, R.id.dictaphoneFragment
                // Добавьте сюда ID ВСЕХ экранов, которые есть в нижнем меню.
                // Это скажет системе, что они все равноправны и стрелка назад между ними не нужна.
            )
        )

        // 2. Связываем NavController с ActionBar (верхней панелью), используя нашу конфигурацию.
        setupActionBarWithNavController(navController, appBarConfiguration)

        // --- КОНЕЦ ИСПРАВЛЕНИЯ ---

        navView.setupWithNavController(navController)
    }
}