package com.example.prottect_yourself

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.example.prottect_yourself.logic.words.WordStorage
import java.util.Locale

class SpeechRecognitionHelper(
    private val context: Context,
    private val onKeywordDetected: (String) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private val TAG = "SpeechHelper"
    // --- ↓↓↓ НОВОЕ ИЗМЕНЕНИЕ ↓↓↓ ---
    // Создаем константу для русского языка, чтобы избежать опечаток
    private val russianLocale = Locale("ru", "RU")
    // --- ↑↑↑ КОНЕЦ ИЗМЕНЕНИЯ ↑↑↑ ---

    init {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(createRecognitionListener())
            Log.d(TAG, "Инициализация прошла успешно. Помощник готов.")
        } else {
            Log.e(TAG, "КРИТИЧЕСКАЯ ОШИБКА: Распознавание речи недоступно на этом устройстве.")
        }
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                Log.d(TAG, "[ЛОГ] onResults: Получен результат!")
                // --- ↓↓↓ НОВОЕ ИЗМЕНЕНИЕ ↓↓↓ ---
                // Используем русский язык для приведения к нижнему регистру
                val keywords = WordStorage.loadWords(context).map { it.lowercase(russianLocale) }
                // --- ↑↑↑ КОНЕЦ ИЗМЕНЕНИЯ ↑↑↑ ---
                Log.d(TAG, "[ЛОГ] Загруженные слова для проверки: $keywords")

                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.isNotEmpty()) {
                    Log.d(TAG, "[ЛОГ] Распознанные варианты: $matches")
                    for (result in matches) {
                        // --- ↓↓↓ НОВОЕ ИЗМЕНЕНИЕ ↓↓↓ ---
                        // И здесь тоже используем русский
                        val recognizedText = result.lowercase(russianLocale)
                        // --- ↑↑↑ КОНЕЦ ИЗМЕНЕНИЯ ↑↑↑ ---

                        val foundKeyword = keywords.find { keyword -> recognizedText.contains(keyword) }

                        if (foundKeyword != null) {
                            onKeywordDetected(foundKeyword)
                            return
                        }
                    }
                    Log.d(TAG, "[ЛОГ] Совпадений не найдено. Перезапускаю прослушивание.")
                } else {
                    Log.d(TAG, "[ЛОГ] Результат пустой. Перезапускаю прослушивание.")
                }
                startListening()
            }

            override fun onError(error: Int) {
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Ошибка аудио"
                    SpeechRecognizer.ERROR_CLIENT -> "Ошибка на стороне клиента"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Недостаточно разрешений"
                    SpeechRecognizer.ERROR_NETWORK -> "Ошибка сети"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Тайм-аут сети"
                    SpeechRecognizer.ERROR_NO_MATCH -> "Нет совпадений"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Распознаватель занят"
                    SpeechRecognizer.ERROR_SERVER -> "Ошибка сервера"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Время ожидания речи истекло"
                    else -> "Неизвестная ошибка"
                }
                Log.e(TAG, "[ЛОГ] onError: Произошла ошибка - $errorMessage (код: $error)")
                startListening()
            }

            override fun onReadyForSpeech(params: Bundle?) { Log.d(TAG, "[ЛОГ] onReadyForSpeech: Микрофон готов, можно говорить.") }
            override fun onBeginningOfSpeech() { Log.d(TAG, "[ЛОГ] onBeginningOfSpeech: Обнаружено начало речи.") }
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { Log.d(TAG, "[ЛОГ] onEndOfSpeech: Обнаружен конец речи.") }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    fun startListening() {
        Log.d(TAG, "[ЛОГ] startListening: Запускаю прослушивание...")
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            // --- ↓↓↓ ГЛАВНОЕ ИЗМЕНЕНИЕ! ↓↓↓ ---
            // Указываем, что мы хотим распознавать именно РУССКИЙ ЯЗЫК
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU")
            // --- ↑↑↑ КОНЕЦ ИЗМЕНЕНИЯ ↑↑↑ ---
        }
        speechRecognizer?.startListening(intent)
    }

    fun destroy() {
        speechRecognizer?.destroy()
        Log.d(TAG, "Помощник уничтожен.")
    }
}
