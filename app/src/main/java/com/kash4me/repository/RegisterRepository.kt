package com.kash4me.repository

import com.kash4me.network.ApiServices

class RegisterRepository(private val apiServices: ApiServices) {


    fun registerUser(params: HashMap<String, Any>) = apiServices.registerUser(params)

}