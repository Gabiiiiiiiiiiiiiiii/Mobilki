package com.example.prottect_yourself.ui.contacts // Убедитесь, что здесь ваш пакет

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.prottect_yourself.databinding.FragmentContactsBinding

class ContactsFragment : Fragment() {

    private var _binding: FragmentContactsBinding? = null
    // Эта строка нужна, чтобы безопасно работать с binding после onCreateView
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Эта строка "надувает" ваш XML-макет и создает объект binding
        _binding = FragmentContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Здесь вы можете начать работать с элементами вашего экрана
        // Например, настроить RecyclerView или обработчик нажатия на кнопку "назад"
        // binding.toolbar.setNavigationOnClickListener { ... }
        // binding.selectedContactsRecyclerView.adapter = ...
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Эта строка очищает ссылку на binding, чтобы избежать утечек памяти
        _binding = null
    }
}