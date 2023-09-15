package com.kash4me.repository

import androidx.lifecycle.LiveData
import com.kash4me.data.local.merchant.MerchantTransactionSummaryEntity
import com.kash4me.data.local.merchant.TransactionsSummaryDao
import com.kash4me.data.models.MerchantTransactionSummaryResponse
import com.kash4me.network.ApiServices
import com.kash4me.utils.network.Resource
import com.kash4me.utils.network.SafeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MerchantTransactionSummaryRepository
@Inject
constructor(
    private val apiServices: ApiServices,
    private val transactionsSummaryDao: TransactionsSummaryDao
) {

    suspend fun updateTransactionSummaryCache(token: String): Flow<Resource<MerchantTransactionSummaryResponse>> {

        return SafeApiCall.makeNetworkCall {
            // Send null if we want to fetch own transactions summary
            apiServices.getTransactionSummary(token = token, merchantId = null)
        }.map {

            when (it) {

                is Resource.Success -> {
                    val cache = MerchantTransactionSummaryEntity(
                        today = it.value.today,
                        weekly = it.value.weekly,
                        monthly = it.value.monthly,
                        isPaymentSetupComplete = it.value.isPaymentSetupComplete
                    )
                    insertTransactionSummaryIntoDb(summary = cache)
                    Resource.Success(it.value) // No need to store data since we are observing live data from db
                }

                is Resource.Loading -> Resource.Loading

                is Resource.Failure -> {
                    Resource.Failure(errorMsg = it.errorMsg, exception = it.exception)
                }

            }

        }.flowOn(Dispatchers.IO)

    }

    suspend fun fetchBranchTransactionSummary(token: String, merchantId: Int?)

            : Flow<Resource<MerchantTransactionSummaryResponse>> {

        return SafeApiCall.makeNetworkCall {
            apiServices.getTransactionSummary(token = token, merchantId = merchantId)
        }.flowOn(Dispatchers.IO)

    }

    private suspend fun insertTransactionSummaryIntoDb(summary: MerchantTransactionSummaryEntity): Long {

        return transactionsSummaryDao.insert(summary = summary)

    }

    fun getTransactionSummaryFromDb(): LiveData<MerchantTransactionSummaryEntity?> {

        return transactionsSummaryDao.getTransactionSummary()

    }

    suspend fun clearTransactionSummaryFromDb() {

        return transactionsSummaryDao.clear()

    }

}