package com.example.prottect_yourself.ui.contacts

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prottect_yourself.databinding.FragmentContactsBinding
import com.example.prottect_yourself.logic.contacts.Contact
import com.example.prottect_yourself.logic.contacts.ContactStorage
import com.example.prottect_yourself.logic.contacts.ContactsAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections

class ContactsFragment : Fragment() {

    private var _binding: FragmentContactsBinding? = null
    // Эта проперти позволяет безопасно работать с binding после onCreateView
    private val binding get() = _binding!!

    // Списки для хранения контактов
    private val allContactsList = mutableListOf<Contact>()
    private val selectedContactsList = mutableListOf<Contact>()

    // Адаптеры для RecyclerView
    private lateinit var allContactsAdapter: ContactsAdapter
    private lateinit var selectedContactsAdapter: ContactsAdapter

    // Современный способ запроса разрешений
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Разрешение получено, загружаем контакты
                loadContacts()
            } else {
                // Пользователь отказал в разрешении. Сообщаем ему.
                Toast.makeText(requireContext(), "Доступ к контактам необходим для работы функции", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // "Надуваем" XML-макет и создаем объект binding
        _binding = FragmentContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Настраиваем RecyclerView
        setupRecyclerViews()
        // Проверяем разрешения и начинаем загрузку контактов
        checkPermissionsAndLoadContacts()
    }

    private fun setupRecyclerViews() {
        // Используем binding для доступа к View. Это безопасно и удобно.
        binding.allContactsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.selectedContactsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Инициализируем адаптер для всех контактов
        allContactsAdapter = ContactsAdapter(mutableListOf()) { contact, isChecked ->
            if (isChecked) {
                moveContact(contact, from = allContactsList, to = selectedContactsList)
            }
        }

        // Инициализируем адаптер для выбранных контактов
        selectedContactsAdapter = ContactsAdapter(mutableListOf()) { contact, isChecked ->
            if (!isChecked) {
                moveContact(contact, from = selectedContactsList, to = allContactsList)
            }
        }

        binding.allContactsRecyclerView.adapter = allContactsAdapter
        binding.selectedContactsRecyclerView.adapter = selectedContactsAdapter
    }

    private fun checkPermissionsAndLoadContacts() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Разрешение уже есть
                loadContacts()
            }
            else -> {
                // Запрашиваем разрешение
                requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
        }
    }

    private fun loadContacts() {
        lifecycleScope.launch(Dispatchers.IO) {
            val contactsMap = mutableMapOf<Long, Contact>()
            val savedContactIds = ContactStorage.loadSelectedContactIds(requireContext())

            val cursor = requireContext().contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER // <-- ЗАПРАШИВАЕМ НОМЕР
                ),
                null,
                null,
                null
            )

            cursor?.use {
                val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val phoneIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER) // <-- ИНДЕКС НОМЕРА

                while (it.moveToNext()) {
                    val id = it.getLong(idIndex)
                    val name = it.getString(nameIndex)
                    val phone = it.getString(phoneIndex)

                    // Добавляем в Map только если контакта с таким ID еще нет,
                    // чтобы у одного контакта был только один (первый попавшийся) номер.
                    if (!contactsMap.containsKey(id)) {
                        val isSelected = savedContactIds.contains(id)
                        contactsMap[id] = Contact(id, name, phone, isSelected)
                    }
                }
            }

            // Разделяем контакты на выбранные и невыбранные сразу после загрузки
            val allContacts = contactsMap.values.toList()
            val (selected, notSelected) = allContacts.partition { it.isSelected }

            withContext(Dispatchers.Main) {
                selectedContactsList.clear()
                selectedContactsList.addAll(selected)
                allContactsList.clear()
                allContactsList.addAll(notSelected)

                // Сортируем оба списка
                selectedContactsList.sortBy { it.name }
                allContactsList.sortBy { it.name }

                updateAdapters()
            }
        }
    }

    private fun moveContact(contact: Contact, from: MutableList<Contact>, to: MutableList<Contact>) {
        from.remove(contact)
        to.add(contact)

        // Сортировка для порядка в списках
        Collections.sort(allContactsList, compareBy { it.name })
        Collections.sort(selectedContactsList, compareBy { it.name })

        // После каждого перемещения мы сохраняем актуальный список выбранных контактов
        ContactStorage.saveContacts(requireContext(), selectedContactsList)

        binding.allContactsRecyclerView.post {
            updateAdapters()
        }
    }

    private fun updateAdapters() {
        allContactsAdapter.updateData(allContactsList)
        selectedContactsAdapter.updateData(selectedContactsList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Очищаем ссылку на binding, чтобы избежать утечек памяти
        _binding = null
    }
}