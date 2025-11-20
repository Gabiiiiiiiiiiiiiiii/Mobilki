package com.example.prottect_yourself.logic.sms

import android.content.Context
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.example.prottect_yourself.logic.contacts.ContactStorage
import com.example.prottect_yourself.logic.email.EmailService // <-- Используем EmailStorage, как создавали ранее

class SmsService {
    fun sendSmsDirectly(context: Context, googleMapsLink: String) {
        // Проверяем разрешение
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {

            // 1. Загружаем шаблон (используем безопасный вызов)
            val messageTemplate = try {
                // В прошлых шагах мы создавали класс EmailStorage, используем его
                EmailService.loadTemplate(context).ifEmpty { "Мне нужна помощь!" }
            } catch (e: Exception) {
                "Мне нужна помощь!"
            }

            val phoneNumbers = ContactStorage.loadSelectedContactNumbers(context)

            // 2. Правильное получение SmsManager для разных версий Android
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                context.getSystemService(SmsManager::class.java)
            } else {
                SmsManager.getDefault()
            }

            val fullMessage = "$messageTemplate\n$googleMapsLink"

            Log.d("SmsService", "Отправляю на ${phoneNumbers.size} номеров. Текст: $fullMessage")

            if (phoneNumbers.isEmpty()) {
                Log.e("SmsService", "Список контактов пуст!")
                return
            }

            phoneNumbers.forEach { number ->
                try {
                    // 3. ВАЖНО: Делим сообщение на части, если оно длинное
                    val parts = smsManager.divideMessage(fullMessage)

                    if (parts.size > 1) {
                        // Если частей > 1, отправляем как мульти-смс
                        smsManager.sendMultipartTextMessage(number, null, parts, null, null)
                        Log.d("SmsService", "Отправлено Multipart SMS на $number")
                    } else {
                        // Если короткое, отправляем обычно
                        smsManager.sendTextMessage(number, null, fullMessage, null, null)
                        Log.d("SmsService", "Отправлено SMS на $number")
                    }
                } catch (e: Exception) {
                    Log.e("SmsService", "Ошибка при отправке на $number: ${e.message}")
                    e.printStackTrace()
                }
            }

            // Toast показываем только если контекст позволяет
            try {
                Toast.makeText(context, "SMS отправлены контактам", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("SmsService", "Не удалось показать Toast из фона")
            }

        } else {
            Log.e("SmsService", "Нет разрешения SEND_SMS")
        }
    }
}