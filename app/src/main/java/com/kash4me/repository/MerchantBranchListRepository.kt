package com.kash4me.repository

import com.kash4me.network.ApiServices

class MerchantBranchListRepository(private val apiServices: ApiServices) {

    fun getMerchantBranchList(token: String, filterOptions: Map<String, String>?) =
        apiServices.getMerchantBranchList(token = token, filterOptions)

}