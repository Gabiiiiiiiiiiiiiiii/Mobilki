package com.example.prottect_yourself.ui.words

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prottect_yourself.databinding.FragmentWordsBinding
import com.example.prottect_yourself.logic.words.WordStorage
import com.example.prottect_yourself.logic.words.WordsAdapter

class WordsFragment : Fragment() {

    private var _binding: FragmentWordsBinding? = null
    private val binding get() = _binding!!

    private lateinit var wordsAdapter: WordsAdapter
    private val wordsList = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWordsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadWords()
        setupAddButton()
    }

    private fun setupRecyclerView() {
        wordsAdapter = WordsAdapter(wordsList) { position ->
            // Этот код выполнится, когда мы нажмем на кнопку удаления в адаптере
            removeWord(position)
        }
        binding.codewordsRecyclerView.apply {
            adapter = wordsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun loadWords() {
        val loadedWords = WordStorage.loadWords(requireContext())
        wordsList.clear()
        wordsList.addAll(loadedWords)
        wordsAdapter.notifyDataSetChanged() // Уведомляем адаптер, что данные загружены
    }

    private fun setupAddButton() {
        binding.addCodewordButton.setOnClickListener {
            val newWord = binding.codewordEditText.text.toString().trim()
            if (newWord.isNotEmpty()) {
                addWord(newWord)
            } else {
                Toast.makeText(requireContext(), "Введите ключевое слово", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addWord(word: String) {
        wordsList.add(word)
        wordsAdapter.notifyItemInserted(wordsList.size - 1) // Эффективное добавление
        binding.codewordEditText.text?.clear() // Очищаем поле ввода
        saveWords() // Сохраняем обновленный список
    }

    private fun removeWord(position: Int) {
        wordsList.removeAt(position)
        wordsAdapter.notifyItemRemoved(position) // Эффективное удаление
        saveWords() // Сохраняем обновленный список
    }

    private fun saveWords() {
        WordStorage.saveWords(requireContext(), wordsList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}