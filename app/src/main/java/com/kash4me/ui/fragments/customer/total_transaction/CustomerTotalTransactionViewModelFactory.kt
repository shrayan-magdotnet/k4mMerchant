package com.kash4me.ui.fragments.customer.total_transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kash4me.repository.CustomerTotalCashBackRepository

class CustomerTotalTransactionViewModelFactory(private val customerTotalCashBackRepository: CustomerTotalCashBackRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CustomerTotalTransactionViewModel(customerTotalCashBackRepository) as T
    }
}