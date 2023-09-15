package com.kash4me.ui.fragments.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kash4me.repository.RegisterRepository

class RegisterViewModelFactory(private val registerRepository: RegisterRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RegisterViewModel(registerRepository) as T
    }
}