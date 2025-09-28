package com.example.prottect_yourself.ui.email

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.prottect_yourself.databinding.FragmentEmailBinding

class EmailFragment : Fragment() {

    private var _binding: FragmentEmailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Связываем Kotlin-код с XML-макетом fragment_email.xml
        _binding = FragmentEmailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Обработчик для кнопки сохранения шаблона текста
        binding.saveTextTemplateButton.setOnClickListener {
            val templateText = binding.textTemplate.text.toString()
            // Сюда добавьте вашу логику сохранения текста
        }


        // Здесь можно настраивать логику экрана.
        // Например, добавить обработчик нажатия на кнопку сохранения:
        binding.saveEmailButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            // Логика сохранения email...
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Очищаем ссылку на binding для предотвращения утечек памяти
        _binding = null
    }
}