package com.kash4me.ui.fragments.customer.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kash4me.repository.MerchantsRepository

class CustomerSearchViewModelFactory(private val nearByMerchantsRepository: MerchantsRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CustomerSearchViewModel(nearByMerchantsRepository) as T
    }
}