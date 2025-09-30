package com.example.prottect_yourself.logic.sms

import android.content.Context
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.example.prottect_yourself.logic.contacts.ContactStorage

class SmsService {
    private fun sendSmsDirectly(context: Context, message: String) {
        // Сначала нужно запросить разрешение SEND_SMS у пользователя во время выполнения
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            val phoneNumbers = ContactStorage.loadSelectedContactNumbers(context)
            val smsManager = context.getSystemService(SmsManager::class.java)

            phoneNumbers.forEach { number ->
                try {
                    smsManager.sendTextMessage(number, null, message, null, null)
                } catch (e: Exception) {
                    // Обработка ошибок
                    e.printStackTrace()
                }
            }
            Toast.makeText(context, "Попытка отправки SMS завершена", Toast.LENGTH_SHORT).show()
        } else {
            // Здесь должен быть запуск запроса разрешения
            // requestSmsPermissionLauncher.launch(android.Manifest.permission.SEND_SMS)
        }
    }
}