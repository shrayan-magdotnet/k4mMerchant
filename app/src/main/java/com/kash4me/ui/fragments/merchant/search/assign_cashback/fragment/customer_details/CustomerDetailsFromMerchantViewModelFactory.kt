package com.kash4me.ui.fragments.merchant.search.assign_cashback.fragment.customer_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kash4me.repository.CustomerDetailsFromMerchantRepository

class CustomerDetailsFromMerchantViewModelFactory(private val customerDetailsFromMerchantRepository: CustomerDetailsFromMerchantRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CustomerDetailsFromMerchantViewModel(customerDetailsFromMerchantRepository) as T

    }
}