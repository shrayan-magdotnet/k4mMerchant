package com.kash4me.repository

import com.kash4me.data.models.ActiveCashbackSettings
import com.kash4me.data.models.CashBackAmountResponse
import com.kash4me.data.models.merchant.accept_kash4me_payment.AcceptKash4mePaymentRequest
import com.kash4me.data.models.merchant.accept_kash4me_payment.AcceptKash4mePaymentResponse
import com.kash4me.data.models.merchant.assign_cashback.AssignCashbackRequest
import com.kash4me.data.models.merchant.calculate_cashback.CalculateCashbackRequest
import com.kash4me.data.models.merchant.cashback_code.SendCashbackCodeRequest
import com.kash4me.data.models.merchant.cashback_code.SendCashbackCodeResponse
import com.kash4me.data.models.merchant.purchase_return.ReturnPurchaseRequest
import com.kash4me.data.models.merchant.purchase_return.ReturnPurchaseResponse
import com.kash4me.data.models.merchant.rollback_transaction.RollbackTransactionRequest
import com.kash4me.network.ApiServices
import com.kash4me.utils.SessionManager
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.network.Resource
import com.kash4me.utils.network.SafeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class CashBackRepository @Inject constructor(
    private val apiServices: ApiServices,
    private val sessionManager: SessionManager
) {

    fun addCashBack(merchantShopID: Int, token: String, params: HashMap<String, Any>) =
        apiServices.addCashBack(merchantShopID, token, params)

    fun getCashbackValue(token: String, params: HashMap<String, Any>) =
        apiServices.getCashBackInfo(token, params)

    suspend fun calculateCashbackAmount(request: CalculateCashbackRequest): Flow<Resource<CashBackAmountResponse>> {

        return SafeApiCall.makeNetworkCall {
            apiServices.calculateCashbackAmount(
                token = sessionManager.fetchAuthToken().getEmptyIfNull(),
                request = request
            )
        }.flowOn(Dispatchers.IO)

    }

    fun getCashbackValue(token: String, request: AssignCashbackRequest) =
        apiServices.getCashbackValue(token, request)

    fun createCashBackTransaction(
        merchantShopID: Int,
        token: String,
        params: HashMap<String, Any>
    ) =
        apiServices.createCashBackTransaction(merchantShopID, token, params)

    fun rollbackTransaction(token: String, request: RollbackTransactionRequest) =
        apiServices.rollbackTransaction(token = token, request = request)

    fun getActiveCashBackSetting(merchantShopID: Int, token: String) =
        apiServices.getActiveCashBackSetting(merchantShopID, token)

    suspend fun fetchActiveCashbackSettings(merchantShopId: Int): Flow<Resource<ActiveCashbackSettings>> {

        return SafeApiCall.makeNetworkCall {
            apiServices.fetchActiveCashbackSettings(
                token = sessionManager.fetchAuthToken().getEmptyIfNull(),
                merchantShopId = merchantShopId
            )
        }.flowOn(Dispatchers.IO)

    }

    suspend fun sendCashbackCode(token: String, request: SendCashbackCodeRequest)

            : Flow<Resource<SendCashbackCodeResponse>> {

        return SafeApiCall.makeNetworkCall {
            apiServices.sendCashbackCode(token = token, request = request)
        }.flowOn(Dispatchers.IO)

    }

    suspend fun returnPurchase(token: String, request: ReturnPurchaseRequest)

            : Flow<Resource<ReturnPurchaseResponse>> {

        return SafeApiCall.makeNetworkCall {
            apiServices.returnPurchase(token = token, request = request)
        }.flowOn(Dispatchers.IO)

    }

    suspend fun acceptKash4mePayment(token: String, request: AcceptKash4mePaymentRequest)

            : Flow<Resource<AcceptKash4mePaymentResponse>> {

        return SafeApiCall.makeNetworkCall {
            apiServices.acceptKash4mePayment(token = token, request = request)
        }.flowOn(Dispatchers.IO)

    }


}