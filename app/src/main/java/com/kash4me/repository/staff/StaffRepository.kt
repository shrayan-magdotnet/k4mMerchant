package com.kash4me.repository.staff

import com.kash4me.data.models.staff.StaffDetailsResponse
import com.kash4me.data.models.staff.StaffTransactionsResponse
import com.kash4me.network.ApiServices
import com.kash4me.utils.App
import com.kash4me.utils.SessionManager
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.network.Resource
import com.kash4me.utils.network.SafeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class StaffRepository(private val apiServices: ApiServices) {

    private val sessionManager by lazy { SessionManager(App.getContext()!!) }

    suspend fun getTransactions(): Flow<Resource<StaffTransactionsResponse>> {

        val token = sessionManager.fetchAuthToken().getEmptyIfNull()
        return SafeApiCall.makeNetworkCall {
            apiServices.getTransactions(token = token)
        }.flowOn(Dispatchers.IO)

    }

    suspend fun getStaffDetails(): Flow<Resource<StaffDetailsResponse>> {

        val token = sessionManager.fetchAuthToken().getEmptyIfNull()
        return SafeApiCall.makeNetworkCall {
            apiServices.getTransactionDetails(token = token)
        }.flowOn(Dispatchers.IO)

    }
}