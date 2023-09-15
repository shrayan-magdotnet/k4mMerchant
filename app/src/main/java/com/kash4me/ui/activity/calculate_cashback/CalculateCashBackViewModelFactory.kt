package com.kash4me.ui.activity.calculate_cashback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kash4me.repository.CashBackRepository

class CalculateCashBackViewModelFactory(private val cashBackRepository: CashBackRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CalculateCashBackViewModel(cashBackRepository) as T
    }
}