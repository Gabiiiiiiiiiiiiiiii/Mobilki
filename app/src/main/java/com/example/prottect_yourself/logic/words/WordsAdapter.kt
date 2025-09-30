package com.example.prottect_yourself.logic.words

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.prottect_yourself.R

class WordsAdapter(
    private val words: MutableList<String>,
    private val onDeleteClick: (Int) -> Unit // Лямбда для обработки удаления
) : RecyclerView.Adapter<WordsAdapter.WordViewHolder>() {

    class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.codewordTextView)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_codeword, parent, false)
        return WordViewHolder(view)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val word = words[position]
        holder.textView.text = word
        holder.deleteButton.setOnClickListener {
            onDeleteClick(holder.adapterPosition) // Сообщаем фрагменту, что нужно удалить элемент
        }
    }

    override fun getItemCount(): Int = words.size
}