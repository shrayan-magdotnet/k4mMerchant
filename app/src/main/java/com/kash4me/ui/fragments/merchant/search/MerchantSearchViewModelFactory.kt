package com.kash4me.ui.fragments.merchant.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kash4me.repository.MerchantCustomerListRepository

class MerchantSearchViewModelFactory(private val merchantCustomerListRepository: MerchantCustomerListRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MerchantSearchViewModel(merchantCustomerListRepository) as T

    }
}