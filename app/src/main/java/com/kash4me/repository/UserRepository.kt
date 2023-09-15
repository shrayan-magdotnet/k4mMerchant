package com.kash4me.repository

import com.kash4me.data.models.customer.delete.DeleteCustomerResponse
import com.kash4me.data.models.merchant.delete.DeleteMerchantResponse
import com.kash4me.data.models.user.CountryResponse
import com.kash4me.data.models.user.TagResponse
import com.kash4me.data.models.user.fee_settings.FeeSettingsResponse
import com.kash4me.data.models.user.feedback.FeedbackRequest
import com.kash4me.data.models.user.feedback.FeedbackResponse
import com.kash4me.data.models.user.notification_settings.NotificationSettingsResponse
import com.kash4me.data.models.user.notification_settings.UpdateNotificationSettingsRequest
import com.kash4me.data.models.user.timezone.TimezoneResponse
import com.kash4me.network.ApiServices
import com.kash4me.utils.App
import com.kash4me.utils.FeeType
import com.kash4me.utils.SessionManager
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.network.Resource
import com.kash4me.utils.network.SafeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Call
import javax.inject.Inject

class UserRepository @Inject constructor(private val apiServices: ApiServices) {

    private val sessionManager by lazy { SessionManager(App.getContext()!!) }

    fun getInfoBox(token: String) = apiServices.getInfoBox(token = token)

    fun getAvailableCountries(token: String): Call<CountryResponse> {
        return apiServices.getAvailableCountries(token = token)
    }

    suspend fun getAvailableTags(): Flow<Resource<TagResponse>> {
        val token = sessionManager.fetchAuthToken().getEmptyIfNull()
        return SafeApiCall.makeNetworkCall {
            apiServices.getAvailableTags(token = token)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getNotificationSettings(): Flow<Resource<NotificationSettingsResponse>> {
        val token = sessionManager.fetchAuthToken().getEmptyIfNull()
        return SafeApiCall.makeNetworkCall {
            apiServices.getNotificationSettings(token = token)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun updateNotificationSettings(request: UpdateNotificationSettingsRequest): Flow<Resource<NotificationSettingsResponse>> {
        val token = sessionManager.fetchAuthToken().getEmptyIfNull()
        return SafeApiCall.makeNetworkCall {
            apiServices.updateNotificationSettings(token = token, request = request)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun sendFeedback(request: FeedbackRequest): Flow<Resource<FeedbackResponse>> {
        val token = sessionManager.fetchAuthToken().getEmptyIfNull()
        return SafeApiCall.makeNetworkCall {
            apiServices.sendFeedback(token = token, request = request)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getFeeSettings(feeType: FeeType): Flow<Resource<FeeSettingsResponse>> {
        val token = sessionManager.fetchAuthToken().getEmptyIfNull()
        return SafeApiCall.makeNetworkCall {
            apiServices.getFeeSettings(token = token, feeType = feeType.value)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getTimezones(countryCode: String): Flow<Resource<TimezoneResponse>> {
        val token = sessionManager.fetchAuthToken().getEmptyIfNull()
        return SafeApiCall.makeNetworkCall {
            apiServices.getTimezones(token = token, countryCode = countryCode)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun deleteMerchantAccount(): Flow<Resource<DeleteMerchantResponse>> {
        val token = sessionManager.fetchAuthToken().getEmptyIfNull()
        return SafeApiCall.makeNetworkCall {
            apiServices.deleteMerchantAccount(token = token)
        }.flowOn(Dispatchers.IO)
    }


    suspend fun deleteCustomerAccount(): Flow<Resource<DeleteCustomerResponse>> {
        val token = sessionManager.fetchAuthToken().getEmptyIfNull()
        return SafeApiCall.makeNetworkCall {
            apiServices.deleteCustomerAccount(token = token)
        }.flowOn(Dispatchers.IO)
    }

}