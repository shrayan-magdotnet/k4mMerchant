package com.kash4me.repository

import com.kash4me.network.ApiServices

class PostCustomerDetailsRepository(private val apiServices: ApiServices) {

    fun postCustomerDetails(token: String, params: HashMap<String, String>) =
        apiServices.postCustomerDetails(token, params)

}