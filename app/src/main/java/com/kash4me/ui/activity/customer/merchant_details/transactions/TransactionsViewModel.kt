package com.kash4me.ui.activity.customer.merchant_details.transactions

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.kash4me.data.models.customer.transactions_according_to_merchant.TransactionsAccordingToMerchantResponse
import com.kash4me.data.models.customer.transactions_according_to_merchant.transaction_details.TransactionDetailsForReturningPurchase
import com.kash4me.repository.CustomerTransactionsRepository
import com.kash4me.utils.SessionManager
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.network.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel
@Inject constructor(
    private val repository: CustomerTransactionsRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    fun getTransactionsAccordingToMerchant(
        merchantId: Int
    ): MutableLiveData<Resource<TransactionsAccordingToMerchantResponse>> {

        var result = MutableLiveData<Resource<TransactionsAccordingToMerchantResponse>>()

        viewModelScope.launch {

            result = repository.getTransactions(
                token = sessionManager.fetchAuthToken().getEmptyIfNull(),
                merchantId = merchantId
            ).asLiveData() as MutableLiveData<Resource<TransactionsAccordingToMerchantResponse>>

        }

        return result

    }

    fun getTransactionDetails(
        transactionId: Int
    ): MutableLiveData<Resource<TransactionDetailsForReturningPurchase>> {

        var result = MutableLiveData<Resource<TransactionDetailsForReturningPurchase>>()

        viewModelScope.launch {

            result = repository.getTransactionDetails(
                token = sessionManager.fetchAuthToken().getEmptyIfNull(),
                transactionId = transactionId
            ).asLiveData() as MutableLiveData<Resource<TransactionDetailsForReturningPurchase>>

        }

        return result

    }

}