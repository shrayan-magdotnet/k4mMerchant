package com.kash4me.repository

import com.kash4me.data.models.BusinessInfoResponse
import com.kash4me.data.models.merchant.update_profile.MerchantProfileUpdateResponse
import com.kash4me.network.ApiServices
import com.kash4me.utils.network.Resource
import com.kash4me.utils.network.SafeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Call
import javax.inject.Inject

class PostMerchantDetailsRepository @Inject constructor(private val apiServices: ApiServices) {

    /**
     * Must hit postBusinessUserDetails first then only postBusinessDetails
     */

    suspend fun postBusinessUserDetails(
        token: String,
        params: HashMap<String, Any>
    ): Flow<Resource<BusinessInfoResponse>> {
        return SafeApiCall.makeNetworkCall {
            apiServices.postBusinessUserDetails(token, params)
        }.flowOn(Dispatchers.IO)
    }

    fun updateMerchantDetails(
        token: String, params: HashMap<String, Any?>, merchantShopID: Int
    ): Call<MerchantProfileUpdateResponse> {
        return apiServices.updateMerchantDetailsInRegistration(token, params, merchantShopID)
    }

//    fun postBusinessDetails(token: String, params: HashMap<String, String>) =
//        apiServices.postBusinessDetails(token, params)


}