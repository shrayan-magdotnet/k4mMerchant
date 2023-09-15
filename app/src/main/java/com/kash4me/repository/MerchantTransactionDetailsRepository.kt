package com.kash4me.repository

import com.kash4me.network.ApiServices

class MerchantTransactionDetailsRepository(private val apiServices: ApiServices) {

    fun getMerchantTransactionDetails(
        token: String, filterOptions: HashMap<String, Any>,
    ) =
        apiServices.getMerchantTransactionDetails(token = token, filterOptions = filterOptions)

}