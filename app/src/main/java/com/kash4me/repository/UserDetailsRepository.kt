package com.kash4me.repository

import com.kash4me.data.models.customer.customer_details.CustomerDetailsResponse
import com.kash4me.data.models.customer.update_profile.CustomerProfileUpdateRequest
import com.kash4me.data.models.merchant.profile.MerchantProfileResponse
import com.kash4me.data.models.merchant.update_profile.MerchantProfileUpdateRequest
import com.kash4me.data.models.merchant.update_profile.MerchantProfileUpdateResponse
import com.kash4me.data.models.request.RefreshAccessTokenRequest
import com.kash4me.network.ApiServices
import com.kash4me.utils.network.Resource
import com.kash4me.utils.network.SafeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MultipartBody
import retrofit2.Call
import javax.inject.Inject


class UserDetailsRepository @Inject constructor(private val apiServices: ApiServices) {

    fun fetchCustomerDetails(token: String) = apiServices.fetchCustomerDetails(token)

    fun updateCustomerDetails(token: String, params: HashMap<String, String>) =
        apiServices.updateCustomerDetails(token, params)

    fun updateCustomerDetails(token: String, request: CustomerProfileUpdateRequest) =
        apiServices.updateCustomerDetails(token, request)

    suspend fun updateCustomerDetails(token: String, params: HashMap<String, Any?>)

            : Flow<Resource<CustomerDetailsResponse?>> {

        return SafeApiCall.makeNetworkCall {
            apiServices.updateCustomerDetails(token = token, params = params)
        }.flowOn(Dispatchers.IO)

    }

    fun fetchMerchantDetails(token: String) =
        apiServices.fetchMerchantDetails(token)

    fun fetchMerchantProfileDetails(token: String) =
        apiServices.fetchMerchantProfileDetails(token)

    fun updateMerchantDetails(token: String, params: HashMap<String, Any>, merchantShopID: Int) =
        apiServices.updateMerchantDetails(token, params, merchantShopID)

    fun updateMerchantProfileDetails(
        token: String,
        params: HashMap<String, Any?>,
        merchantShopID: Int
    ): Call<MerchantProfileResponse> {
        return apiServices.updateMerchantProfileDetails(
            token = token, merchantShopID = merchantShopID, params = params
        )
    }

    suspend fun updateMerchantProfile(
        token: String,
        merchantShopID: Int,
        params: HashMap<String, Any?>
    )
            : Flow<Resource<MerchantProfileResponse?>> {

        return SafeApiCall.makeNetworkCall {
            apiServices.updateMerchantProfile(
                token = token, merchantShopID = merchantShopID, params = params
            )
        }.flowOn(Dispatchers.IO)

    }

    suspend fun updateMerchantProfileLogo(
        token: String,
        merchantShopID: Int,
        imagePart: MultipartBody.Part
    )
            : Flow<Resource<MerchantProfileResponse>> {

        return SafeApiCall.makeNetworkCall {
            apiServices.updateMerchantProfileLogo(
                token = token, merchantShopID = merchantShopID, logo = imagePart
            )
        }.flowOn(Dispatchers.IO)

    }

    fun updateMerchantDetails(
        token: String, request: MerchantProfileUpdateRequest, merchantShopID: Int
    ): Call<MerchantProfileUpdateResponse> {
        return apiServices.updateMerchantDetails(token, request, merchantShopID)
    }

    fun refreshAccessToken(request: RefreshAccessTokenRequest) =
        apiServices.refreshAccessToken(request = request)

}