package com.example.prottect_yourself.ui.dictaphone

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DictaphoneViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Dictaphone Fragment"
    }
    val text: LiveData<String> = _text
}