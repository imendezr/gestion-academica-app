package com.example.gestionacademicaapp.ui.inicio

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class InicioViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Bienvenido"
    }
    val text: LiveData<String> = _text
}