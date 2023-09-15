package com.kash4me.repository

import com.kash4me.data.models.merchant.rollback_transaction.RollbackTransactionRequest
import com.kash4me.network.ApiServices

class CustomerDetailsFromMerchantRepository(private val apiServices: ApiServices) {

    fun getCustomerDetailsFromMerchant(token: String, customerId: Int) =
        apiServices.getCustomerDetailsFromMerchant(token = token, customerId = customerId)

    fun rollbackTransaction(token: String, request: RollbackTransactionRequest) =
        apiServices.rollbackTransaction(token = token, request = request)

}