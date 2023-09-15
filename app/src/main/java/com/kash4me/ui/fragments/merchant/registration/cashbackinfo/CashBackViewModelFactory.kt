package com.kash4me.ui.fragments.merchant.registration.cashbackinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kash4me.repository.CashBackRepository

class CashBackViewModelFactory(private val cashBackRepository: CashBackRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CashBackViewModel(cashBackRepository) as T
    }
}