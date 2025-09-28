package com.example.prottect_yourself.ui.words

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.prottect_yourself.databinding.FragmentWordsBinding

class WordsFragment : Fragment() {

    private var _binding: FragmentWordsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Связываем Kotlin-код с XML-макетом fragment_words.xml
        _binding = FragmentWordsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Здесь можно настраивать логику экрана.
        // Например, обработать нажатие на кнопку "Добавить":
        binding.addCodewordButton.setOnClickListener {
            val newWord = binding.codewordEditText.text.toString()
            // Логика добавления нового слова в список...
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Очищаем ссылку на binding для предотвращения утечек памяти
        _binding = null
    }
}