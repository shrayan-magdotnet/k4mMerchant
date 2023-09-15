package com.kash4me.ui.activity.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kash4me.repository.UserDetailsRepository

class SplashViewModelFactory(private val customerDetailsRepository: UserDetailsRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SplashViewModel(customerDetailsRepository) as T
    }
}