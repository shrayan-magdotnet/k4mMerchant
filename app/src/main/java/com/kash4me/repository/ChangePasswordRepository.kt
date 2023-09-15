package com.kash4me.repository

import com.kash4me.data.models.SuccessResponse
import com.kash4me.data.models.user.change_password.ChangePasswordRequest
import com.kash4me.network.ApiServices
import com.kash4me.utils.network.Resource
import com.kash4me.utils.network.SafeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class ChangePasswordRepository(private val apiServices: ApiServices) {

    suspend fun changePassword(
        token: String, request: ChangePasswordRequest
    ): Flow<Resource<SuccessResponse>> {
        return SafeApiCall.makeNetworkCall {
            apiServices.changePassword(token = token, request = request)
        }.flowOn(Dispatchers.IO)
    }

}