package com.example.prottect_yourself.logic.words

import android.content.Context
import java.io.File

object WordStorage {

    private const val FILE_NAME = "keywords.txt"

    // Метод для сохранения списка слов в файл
    fun saveWords(context: Context, words: List<String>) {
        val file = File(context.filesDir, FILE_NAME)
        file.printWriter().use { out ->
            words.forEach { word ->
                out.println(word) // Каждое слово на новой строке
            }
        }
    }

    // Метод для загрузки слов из файла
    fun loadWords(context: Context): MutableList<String> {
        val file = File(context.filesDir, FILE_NAME)
        return if (file.exists()) {
            file.readLines().toMutableList()
        } else {
            mutableListOf() // Если файла нет, возвращаем пустой список
        }
    }
}