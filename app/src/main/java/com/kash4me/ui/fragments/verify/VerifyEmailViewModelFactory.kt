package com.kash4me.ui.fragments.verify

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kash4me.repository.VerifyEmailRepository

class VerifyEmailViewModelFactory(private val verifyEmailRepository: VerifyEmailRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VerifyEmailViewModel(verifyEmailRepository) as T
    }
}