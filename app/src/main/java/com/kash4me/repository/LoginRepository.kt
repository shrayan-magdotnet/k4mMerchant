package com.kash4me.repository

import com.kash4me.network.ApiServices
import javax.inject.Inject

class LoginRepository @Inject constructor(private val apiServices: ApiServices) {


    fun loginCustomer(request: HashMap<String, String>) = apiServices.login(request = request)
    fun loginMerchant(request: HashMap<String, String>) = apiServices.login(request = request)

    fun fetchMerchantDetailsForCashBack(token: String) =
        apiServices.fetchMerchantDetailsForCashBack(token)

}