package com.kash4me.ui.activity.forget_password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kash4me.repository.ForgetPasswordRepository

class ForgetPasswordViewModelFactory(private val forgetPasswordRepository: ForgetPasswordRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ForgetPasswordViewModel(forgetPasswordRepository) as T
    }
}