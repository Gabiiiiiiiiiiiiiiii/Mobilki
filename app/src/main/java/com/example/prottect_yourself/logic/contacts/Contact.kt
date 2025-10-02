package com.example.prottect_yourself.logic.contacts

data class Contact(
    val id: Long,          // Уникальный ID контакта
    val name: String,      // Имя контакта
    var phone: String?,    // Номер телефона
    var isSelected: Boolean = false // Состояние выбора
)