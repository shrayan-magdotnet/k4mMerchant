package com.kash4me.ui.fragments.customer.basicinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kash4me.repository.PostCustomerDetailsRepository
import com.kash4me.repository.UserRepository

class CustomerBasicInfoViewModelFactory(
    private val postCustomerDetailsRepository: PostCustomerDetailsRepository,
    private val userRepository: UserRepository
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CustomerBasicInfoViewModel(
            postCustomerDetailsRepository = postCustomerDetailsRepository,
            userRepository = userRepository
        ) as T
    }
}