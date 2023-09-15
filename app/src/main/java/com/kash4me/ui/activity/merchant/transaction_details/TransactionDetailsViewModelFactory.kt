package com.kash4me.ui.activity.merchant.transaction_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kash4me.repository.MerchantTransactionDetailsRepository

class TransactionDetailsViewModelFactory(private val merchantTransactionDetailsRepository: MerchantTransactionDetailsRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TransactionDetailsViewModel(merchantTransactionDetailsRepository) as T
    }
}