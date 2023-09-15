package com.kash4me.repository

import com.kash4me.network.ApiServices

class MerchantCustomerListRepository(private val apiServices: ApiServices) {

    fun getMerchantCustomerList(
        token: String,
        filterOptions: Map<String, String>,
    ) =
        apiServices.getMerchantCustomerList(token, filterOptions)

}