package com.example.prottect_yourself.ui.dictaphone

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.prottect_yourself.databinding.FragmentDictaphoneBinding

class DictaphoneFragment : Fragment() {

    private var _binding: FragmentDictaphoneBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Связываем Kotlin-код с XML-макетом fragment_dictaphone.xml
        _binding = FragmentDictaphoneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Здесь можно настраивать логику для списка записей.
        // Например, установить адаптер для RecyclerView:
        // binding.recordingsRecyclerView.adapter = ...
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Очищаем ссылку на binding для предотвращения утечек памяти
        _binding = null
    }
}