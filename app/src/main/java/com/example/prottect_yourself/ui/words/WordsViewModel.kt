package com.example.prottect_yourself.ui.words

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class WordsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Words Fragment"
    }
    val text: LiveData<String> = _text
}