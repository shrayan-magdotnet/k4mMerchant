package com.kash4me.ui.fragments.merchant.registration.basicinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kash4me.repository.PostMerchantDetailsRepository
import com.kash4me.repository.UserDetailsRepository
import com.kash4me.repository.UserRepository

class MerchantBasicInfoViewModelFactory(
    private val postMerchantDetailsRepository: PostMerchantDetailsRepository,
    private val userRepository: UserRepository,
    private val userDetailsRepository: UserDetailsRepository
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MerchantBasicInfoViewModel(
            postMerchantDetailsRepository,
            userRepository,
            userDetailsRepository
        ) as T
    }
}