package com.kash4me.repository

import android.content.Context
import com.kash4me.data.models.payment_gateway.ConnectYourBankResponse
import com.kash4me.data.models.payment_gateway.PaymentGatewayResponse
import com.kash4me.data.models.payment_gateway.PaymentInformationResponse
import com.kash4me.network.ApiServices
import com.kash4me.utils.PaymentMethod
import com.kash4me.utils.SessionManager
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.network.Resource
import com.kash4me.utils.network.SafeApiCall
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class PaymentRepository @Inject constructor(
    val apiServices: ApiServices,
    @ApplicationContext val applicationContext: Context
) {

    private val mSessionManager by lazy { SessionManager(context = applicationContext) }

    suspend fun connectBankAccount(): Flow<Resource<ConnectYourBankResponse>> {
        val token = mSessionManager.fetchAuthToken().getEmptyIfNull()
        return SafeApiCall.makeNetworkCall {
            apiServices.connectBank(token = token, paymentMethod = PaymentMethod.VOPAY_BANK)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getPaymentInformation(paymentMethod: PaymentMethod): Flow<Resource<List<PaymentInformationResponse>>> {
        val token = mSessionManager.fetchAuthToken().getEmptyIfNull()
        return SafeApiCall.makeNetworkCall {
            apiServices.getPaymentInformation(token = token, paymentMethod = paymentMethod)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun createPaymentInformation(request: HashMap<String, Any>): Flow<Resource<List<PaymentInformationResponse>>> {
        val token = mSessionManager.fetchAuthToken().getEmptyIfNull()
        return SafeApiCall.makeNetworkCall {
            apiServices.createPaymentInformation(token = token, request = request)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun updatePaymentInformation(
        paymentId: Int,
        request: HashMap<String, Any>
    ): Flow<Resource<List<PaymentInformationResponse>>> {
        val token = mSessionManager.fetchAuthToken().getEmptyIfNull()
        return SafeApiCall.makeNetworkCall {
            apiServices.updatePaymentInformation(
                token = token, request = request, paymentId = paymentId
            )
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getPaymentGateways(): Flow<Resource<List<PaymentGatewayResponse>>> {
        val token = mSessionManager.fetchAuthToken().getEmptyIfNull()
        return SafeApiCall.makeNetworkCall {
            apiServices.getPaymentGateways(token = token)
        }.flowOn(Dispatchers.IO)
    }


}