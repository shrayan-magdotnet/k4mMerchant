package com.kash4me.ui.fragments.customer.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kash4me.repository.UserDetailsRepository
import com.kash4me.repository.UserRepository

class CustomerProfileViewModelFactory(
    private val userDetailsRepository: UserDetailsRepository,
    private val userRepository: UserRepository
)

    : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CustomerProfileViewModel(
            userDetailsRepository = userDetailsRepository,
            userRepository = userRepository
        ) as T
    }

}