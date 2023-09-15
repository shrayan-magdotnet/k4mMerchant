package com.kash4me.repository

import com.kash4me.data.models.merchant.sub_user_settings.SubUserResponse
import com.kash4me.data.models.merchant.sub_user_settings.add_sub_user.AddSubUserRequest
import com.kash4me.data.models.merchant.sub_user_settings.delete_sub_user.DeleteSubUserResponse
import com.kash4me.data.models.merchant.sub_user_settings.reset_staff_password.ResetStaffPasswordRequest
import com.kash4me.data.models.merchant.sub_user_settings.reset_staff_password.ResetStaffPasswordResponse
import com.kash4me.network.ApiServices
import com.kash4me.utils.App
import com.kash4me.utils.SessionManager
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.network.Resource
import com.kash4me.utils.network.SafeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class SubUserSettingsRepository(private val apiServices: ApiServices) {

    private val sessionManager by lazy { SessionManager(App.getContext()!!) }

    suspend fun getSubUsers(): Flow<Resource<List<SubUserResponse>>> {

        val token = sessionManager.fetchAuthToken().getEmptyIfNull()
        return SafeApiCall.makeNetworkCall {
            apiServices.getSubUsers(token = token)
        }.flowOn(Dispatchers.IO)

    }

    suspend fun addSubUser(request: AddSubUserRequest): Flow<Resource<List<SubUserResponse>?>> {

        val token = sessionManager.fetchAuthToken().getEmptyIfNull()
        return SafeApiCall.makeNetworkCall {
            apiServices.addSubUser(token = token, request = request)
        }.flowOn(Dispatchers.IO)

    }

    suspend fun deleteSubUser(shopId: Int): Flow<Resource<DeleteSubUserResponse?>> {

        val token = sessionManager.fetchAuthToken().getEmptyIfNull()
        return SafeApiCall.makeNetworkCall {
            apiServices.deleteSubUser(token = token, id = shopId)
        }.flowOn(Dispatchers.IO)

    }

    suspend fun resetStaffPassword(request: ResetStaffPasswordRequest): Flow<Resource<ResetStaffPasswordResponse?>> {

        val token = sessionManager.fetchAuthToken().getEmptyIfNull()
        return SafeApiCall.makeNetworkCall {
            apiServices.resetStaffUserPassword(token = token, request = request)
        }.flowOn(Dispatchers.IO)

    }

}