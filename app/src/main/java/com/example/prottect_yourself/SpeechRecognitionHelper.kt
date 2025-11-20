package com.example.prottect_yourself

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
    private val russianLocale = Locale("ru", "RU")

    private var isListening = false
    private val handler = Handler(Looper.getMainLooper())

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var originalNotificationVolume = 0
    private var originalSystemVolume = 0
    private var isMuted = false

    init {
        initializeRecognizer()
    }

    private fun initializeRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(createRecognitionListener())
            Log.d(TAG, "Распознаватель инициализирован")
        } else {
            Log.e(TAG, "Распознавание речи недоступно")
        }
    }

    private fun muteSound() {
        if (isMuted) return
        try {
            originalNotificationVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
            originalSystemVolume = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM)

            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, 0)
            audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0)

            isMuted = true
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка глушения звука: ${e.message}")
        }
    }

    private fun unmuteSound() {
        if (!isMuted) return
        try {
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, originalNotificationVolume, 0)
            audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, originalSystemVolume, 0)
            isMuted = false
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка восстановления звука: ${e.message}")
        }
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                // Загружаем ключевые слова
                val keywords = WordStorage.loadWords(context).map { it.lowercase(russianLocale) }

                if (matches != null && matches.isNotEmpty()) {
                    for (result in matches) {
                        val recognizedText = result.lowercase(russianLocale)
                        val foundKeyword = keywords.find { keyword -> recognizedText.contains(keyword) }

                        if (foundKeyword != null) {
                            Log.i(TAG, "КЛЮЧЕВОЕ СЛОВО НАЙДЕНО: $foundKeyword")

                            // ВАЖНОЕ ИЗМЕНЕНИЕ: Сначала включаем звук, чтобы SMS/Сирена были слышны
                            unmuteSound()

                            // Сообщаем сервису о находке
                            onKeywordDetected(foundKeyword)
                        }
                    }
                }
                // Перезапускаем прослушивание
                restartListening()
            }

            override fun onError(error: Int) {
                // Если ошибка "Busy", ждем 500мс, иначе 100мс. 10мс слишком мало.
                val delay = if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) 500L else 100L
                restartListening(delay)
            }

            override fun onEndOfSpeech() {}
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    fun startListening() {
        isListening = true
        muteSound()
        startListeningInternal()
    }

    private fun startListeningInternal() {
        if (!isListening) return

        handler.post {
            try {
                speechRecognizer?.cancel()
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU")
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
                    putExtra("android.speech.extra.DICTATION_MODE", true)
                }
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                restartListening(1000)
            }
        }
    }

    private fun restartListening(delayMillis: Long = 100) {
        if (!isListening) return
        handler.postDelayed({
            startListeningInternal()
        }, delayMillis)
    }

    fun destroy() {
        isListening = false
        handler.removeCallbacksAndMessages(null)
        unmuteSound() // Возвращаем звук при выключении
        speechRecognizer?.destroy()
        speechRecognizer = null
        Log.d(TAG, "Уничтожен")
    }
}