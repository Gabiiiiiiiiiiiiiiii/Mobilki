package com.example.prottect_yourself.logic.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.prottect_yourself.R
import com.example.prottect_yourself.SpeechRecognitionHelper
import com.example.prottect_yourself.logic.sms.SmsService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class VoiceBackgroundService : Service() {

    private var speechHelper: SpeechRecognitionHelper? = null
    private val CHANNEL_ID = "VoiceServiceChannel"
    private val mainHandler = Handler(Looper.getMainLooper())

    // Клиент для получения координат
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, createNotification())

        // Инициализируем GPS
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Инициализируем распознавание в главном потоке
        mainHandler.post {
            initializeSpeechRecognizer()
        }
    }

    private fun initializeSpeechRecognizer() {
        try {
            speechHelper = SpeechRecognitionHelper(this) { keyword ->
                // --- ЭТА ЧАСТЬ СРАБОТАЕТ ПРИ ОБНАРУЖЕНИИ СЛОВА ---
                Log.w("VoiceService", "!!! АТАКА !!! СЛОВО: $keyword")

                // Показываем пользователю, что сработало
                Toast.makeText(applicationContext, "Отправка тревоги...", Toast.LENGTH_LONG).show()

                // Запускаем цепочку: Получить координаты -> Отправить SMS
                getLocationAndSendSms(keyword)
            }

            speechHelper?.startListening()

        } catch (e: Exception) {
            Log.e("VoiceService", "Ошибка запуска: ${e.message}")
        }
    }

    private fun getLocationAndSendSms(keyword: String) {
        // Проверка прав на GPS (формальная, так как мы проверили их при запуске кнопки)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Если прав нет, отправляем SMS без координат
            val smsService = SmsService()
            smsService.sendSmsDirectly(this, "SOS! Ключевое слово: $keyword. Нет доступа к GPS.")
            return
        }

        // Пытаемся получить координаты
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                var googleMapsLink = ""
                if (location != null) {
                    // Формируем ссылку
                    googleMapsLink = "https://maps.google.com/?q=${location.latitude},${location.longitude}"
                } else {
                    googleMapsLink = "Координаты не определены (нет сигнала GPS)."
                }

                // ОТПРАВЛЯЕМ SMS
                val smsService = SmsService()
                // Текст сообщения сформируется внутри SmsService (Шаблон + Ссылка)
                smsService.sendSmsDirectly(this, googleMapsLink)
            }
            .addOnFailureListener {
                // Ошибка GPS
                val smsService = SmsService()
                smsService.sendSmsDirectly(this, "SOS! Ошибка получения GPS.")
            }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mainHandler.post {
            speechHelper?.destroy()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Защита активна")
            .setContentText("Анализ речи...")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Убедитесь, что иконка есть
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Voice Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}