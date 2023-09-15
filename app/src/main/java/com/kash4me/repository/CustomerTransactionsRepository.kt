package com.kash4me.repository

import com.kash4me.data.models.customer.transactions_according_to_merchant.TransactionsAccordingToMerchantResponse
import com.kash4me.data.models.customer.transactions_according_to_merchant.transaction_details.TransactionDetailsForReturningPurchase
import com.kash4me.network.ApiServices
import com.kash4me.utils.network.Resource
import com.kash4me.utils.network.SafeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject


class CustomerTransactionsRepository @Inject constructor(private val apiServices: ApiServices) {

    suspend fun getTransactions(
        token: String, merchantId: Int
    ): Flow<Resource<TransactionsAccordingToMerchantResponse>> {
        return SafeApiCall.makeNetworkCall {
            apiServices.getTransactionsAccordingToMerchant(token = token, merchantId = merchantId)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getTransactionDetails(
        token: String, transactionId: Int
    ): Flow<Resource<TransactionDetailsForReturningPurchase>> {
        return SafeApiCall.makeNetworkCall {
            apiServices.getTransactionDetailsForReturningPurchase(
                token = token,
                transactionId = transactionId
            )
        }.flowOn(Dispatchers.IO)
    }


}