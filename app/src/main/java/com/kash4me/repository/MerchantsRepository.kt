package com.kash4me.repository

import com.kash4me.network.ApiServices

class MerchantsRepository(private val apiServices: ApiServices) {

    fun getMerchants(token: String, filterOptions: Map<String, String>) =
        apiServices.getMerchants(token = token, filterOptions = filterOptions)

}