package com.kash4me.ui.activity.merchant.merchant_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kash4me.repository.UserDetailsRepository
import com.kash4me.repository.UserRepository

class MerchantProfileViewModelFactory(
    private val userDetailsRepository: UserDetailsRepository,
    private val userRepository: UserRepository
)

    : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MerchantProfileViewModel(
            userDetailsRepository = userDetailsRepository,
            userRepository = userRepository
        ) as T
    }

}