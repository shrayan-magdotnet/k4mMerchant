package com.kash4me.repository

import com.kash4me.network.ApiServices
import javax.inject.Inject

class MerchantDetailsWithCustomerInfoRepository @Inject constructor(val apiServices: ApiServices) {

    fun getMerchantDetailsWithCustomerInfo(
        merchantShopID: Int,
        token: String,
        lat: Double,
        lng: Double
    ) =
        apiServices.getMerchantDetailsWithCustomerInfo(merchantShopID, token, lat, lng)

}