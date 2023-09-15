package com.kash4me.repository

import com.kash4me.network.ApiServices

class ForgetPasswordRepository(private val apiServices: ApiServices) {

    fun forgetPassword(params: HashMap<String, String>) = apiServices.forgetPassword(params)

}