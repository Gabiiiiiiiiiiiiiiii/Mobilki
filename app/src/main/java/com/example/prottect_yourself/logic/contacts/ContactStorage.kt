package com.example.prottect_yourself.logic.contacts

import android.content.Context
import com.example.prottect_yourself.logic.contacts.Contact
import java.io.File

object ContactStorage {

    private const val FILE_NAME = "selected_contacts.txt"

    // Метод для сохранения контактов в файл
    fun saveContacts(context: Context, contacts: List<Contact>) {
        val file = File(context.filesDir, FILE_NAME)
        // 'use' автоматически закроет поток после записи
        file.printWriter().use { out ->
            contacts.forEach { contact ->
                // Сохраняем в формате: ID;Имя;НомерТелефона
                out.println("${contact.id};${contact.name};${contact.phone}")
            }
        }
    }

    // Метод для загрузки только ID сохраненных контактов
    // Это нужно для быстрой проверки при основной загрузке
    fun loadSelectedContactIds(context: Context): Set<Long> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) {
            return emptySet() // Если файла нет, возвращаем пустой набор
        }

        return file.readLines().mapNotNull { line ->
            line.split(';').firstOrNull()?.toLongOrNull()
        }.toSet()
    }

    fun loadSelectedContactNumbers(context: Context): Set<String> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) {
            return emptySet() // Возвращаем пустой набор строк
        }

        return file.readLines().mapNotNull { line ->
            line.split(';').getOrNull(2)
        }.toSet()
    }
}