package com.kash4me.ui.activity.change_password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kash4me.repository.ChangePasswordRepository

class ChangePasswordViewModelFactory(private val changePasswordRepository: ChangePasswordRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChangePasswordViewModel(changePasswordRepository) as T
    }
}