package com.kash4me.repository

import androidx.lifecycle.LiveData
import com.kash4me.data.local.AppDatabase
import com.kash4me.data.local.customer.cashback.CashbackEntity
import com.kash4me.data.local.customer.cashback.ShopDetails
import com.kash4me.data.models.customer.claim_coupon.ClaimCouponRequest
import com.kash4me.data.models.customer.claim_coupon.ClaimCouponResponse
import com.kash4me.data.models.request.RequestCashbackQrRequest
import com.kash4me.data.models.response.RequestCashbackQrResponse
import com.kash4me.network.ApiServices
import com.kash4me.utils.network.Resource
import com.kash4me.utils.network.SafeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CustomerCashBackDetailsRepository
@Inject
constructor(
    private val apiServices: ApiServices,
    private val appDatabase: AppDatabase
) {

    fun fetchCustomerCashBackDetails(token: String, filterOptions: Map<String, String>) =
        apiServices.fetchCustomerCashBackDetails(token = token, filterOptions = filterOptions)

    fun getCashbacks(): LiveData<List<CashbackEntity>> {
        return appDatabase.cashbackDao().getAll()
    }

    suspend fun updateCashbacksInCache(token: String): Flow<Resource<Unit>> {

        return SafeApiCall.makeNetworkCall {
            apiServices.fetchCustomerCashbacks(token = token)
        }.flowOn(Dispatchers.IO).map {

            when (it) {

                is Resource.Success -> {
                    appDatabase.cashbackDao()
                        .updateAll(cashbacks = it.value.results?.map { response ->
                            CashbackEntity(
                                count = it.value.count,
                                amountLeft = response.amount_left,
                                amountSpent = response.amount_spent,
                                cashbackAmount = response.cashback_amount,
                                processingAmount = response.processing_amount,
                                goalAmount = response.goalAmount,
                                progressPercent = response.progressPercent,
                                totalProcessing = it.value.totalProcessing,
                                shopDetails = ShopDetails(
                                    address = response.shop_details?.address,
                                    logo = response.shop_details?.logo,
                                    name = response.shop_details?.name,
                                    shopId = response.shop_details?.shop_id,
                                )
                            )
                        })
                    Resource.Success(Unit)
                }

                is Resource.Failure -> it

                is Resource.Loading -> it

            }

        }

    }

    fun requestCashbackQr(token: String, request: RequestCashbackQrRequest) =
        apiServices.requestQr(token = token, request = request)

    suspend fun generateQrCode(
        token: String, request: RequestCashbackQrRequest
    ): Flow<Resource<RequestCashbackQrResponse>> {
        return SafeApiCall.makeNetworkCall {
            apiServices.generateQrCodeToRequestCashback(token = token, request = request)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun claimCoupon(
        token: String, request: ClaimCouponRequest
    ): Flow<Resource<ClaimCouponResponse>> {
        return SafeApiCall.makeNetworkCall {
            apiServices.claimCoupon(token = token, request = request)
        }.flowOn(Dispatchers.IO)
    }

}