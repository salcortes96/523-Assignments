package com.example.ninjaautodetailing.ui.plans

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PlansViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "maint plan"
    }
    val text: LiveData<String> = _text
}