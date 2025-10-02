package com.example.prottect_yourself.logic.contacts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.prottect_yourself.R

class ContactsAdapter(
    private var contacts: MutableList<Contact>,
    // Лямбда-функция для обработки клика. Она передаст контакт и новое состояние чекбокса
    private val onContactChecked: (Contact, Boolean) -> Unit
) : RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>() {

    // ViewHolder хранит ссылки на View-элементы одного item'а
    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.contactNameTextView)
        val checkBox: CheckBox = itemView.findViewById(R.id.contactCheckBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun getItemCount(): Int = contacts.size

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        // Заполняем View данными из модели
        holder.nameTextView.text = contact.name
        // 1. ВАЖНО: Полностью отключаем старый слушатель, чтобы избежать
        // ложных срабатываний при переиспользовании View.
        holder.checkBox.setOnCheckedChangeListener(null)

        // 2. Устанавливаем состояние чекбокса (отмечен или нет) из НАШИХ данных.
        // На этом шаге слушатель НЕ сработает, так как мы его отключили.
        holder.checkBox.isChecked = contact.isSelected

        // 3. Теперь, когда View в правильном состоянии, ВОЗВРАЩАЕМ слушатель на место.
        // Он будет ждать только реальных нажатий от пользователя.
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            // Обновляем состояние в нашей модели, чтобы оно было актуальным
            contact.isSelected = isChecked
            // Вызываем колбэк, чтобы Fragment мог отреагировать на изменение
            onContactChecked(contact, isChecked)
        }
    }

    // Метод для обновления данных в адаптере
    fun updateData(newContacts: List<Contact>) {
        this.contacts.clear()
        this.contacts.addAll(newContacts)
        notifyDataSetChanged() // Уведомляем адаптер, что данные изменились
    }
}