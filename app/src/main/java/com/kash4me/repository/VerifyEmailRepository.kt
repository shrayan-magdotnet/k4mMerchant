package com.kash4me.repository

import com.kash4me.network.ApiServices

class VerifyEmailRepository(private val apiServices: ApiServices) {

    fun verifyEmail(params: HashMap<String, String>) = apiServices.verifyEmail(params)

    fun resendOTP(params: HashMap<String, String>) = apiServices.resendOTP(params)

}