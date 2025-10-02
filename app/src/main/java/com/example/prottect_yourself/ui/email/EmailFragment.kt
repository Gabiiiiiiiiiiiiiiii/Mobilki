package com.example.prottect_yourself.ui.email

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prottect_yourself.databinding.FragmentEmailBinding // <-- ЗАМЕНИТЕ НА ВАШ ПАКЕТ
import com.example.prottect_yourself.logic.email.EmailAdapter
import com.example.prottect_yourself.logic.email.EmailService

class EmailFragment : Fragment() {

    private var _binding: FragmentEmailBinding? = null
    private val binding get() = _binding!!

    private lateinit var emailAdapter: EmailAdapter
    private val emailsList = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadData()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        emailAdapter = EmailAdapter(emailsList) { position ->
            removeEmail(position)
        }
        binding.emailRecipientsRecyclerView.apply {
            adapter = emailAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun loadData() {
        // Загружаем и устанавливаем шаблон
        val template = EmailService.loadTemplate(requireContext())
        binding.textTemplate.setText(template)

        // Загружаем и отображаем список email
        val loadedEmails = EmailService.loadEmails(requireContext())
        emailsList.clear()
        emailsList.addAll(loadedEmails)
        emailAdapter.notifyDataSetChanged()
    }

    private fun setupClickListeners() {
        // Кнопка сохранения шаблона
        binding.saveTextTemplateButton.setOnClickListener {
            val template = binding.textTemplate.text.toString()
            EmailService.saveTemplate(requireContext(), template)
            Toast.makeText(requireContext(), "Шаблон сохранен", Toast.LENGTH_SHORT).show()
        }

        // Кнопка сохранения email
        binding.saveEmailButton.setOnClickListener {
            val newEmail = binding.emailEditText.text.toString().trim()
            if (newEmail.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                addEmail(newEmail)
            } else {
                Toast.makeText(requireContext(), "Введите корректный email-адрес", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addEmail(email: String) {
        emailsList.add(email)
        emailAdapter.notifyItemInserted(emailsList.size - 1)
        binding.emailEditText.text?.clear()
        saveEmails()
    }

    private fun removeEmail(position: Int) {
        emailsList.removeAt(position)
        emailAdapter.notifyItemRemoved(position)
        saveEmails()
    }

    private fun saveEmails() {
        EmailService.saveEmails(requireContext(), emailsList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}