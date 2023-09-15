package com.kash4me.repository

import androidx.lifecycle.LiveData
import com.kash4me.data.local.AppDatabase
import com.kash4me.data.local.customer.total_transactions.CustomerTotalTransactionDetailsEntityV2
import com.kash4me.data.local.customer.total_transactions.ProcessingTransactionEntity
import com.kash4me.data.models.customer.create_transaction.CreateTransactionRequest
import com.kash4me.data.models.customer.on_your_way_transactions.ProcessingTransaction
import com.kash4me.data.models.customer.pay_by_kash4me.PayByKash4meRequest
import com.kash4me.data.models.customer.pay_by_kash4me.PayByKash4meResponse
import com.kash4me.data.models.customer.response.CustomerTransactionsResponseV2
import com.kash4me.data.models.customer.withdraw_amount.WithdrawAmountResponse
import com.kash4me.network.ApiServices
import com.kash4me.utils.SessionManager
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.network.Resource
import com.kash4me.utils.network.SafeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject


class CustomerTotalCashBackRepository
@Inject
constructor(
    private val apiServices: ApiServices,
    private val appDatabase: AppDatabase,
    private val sessionManager: SessionManager
) {

    fun fetchCustomerTotalCashBack(token: String, filterOptions: Map<String, String>) =
        apiServices.fetchCustomerTotalCashBack(token = token, filterOptions = filterOptions)

    suspend fun updateProcessingTransactionsInCache(): Flow<Resource<Unit>> {

        return SafeApiCall.makeNetworkCall {
            val token = sessionManager.fetchAuthToken().getEmptyIfNull()
            apiServices.getProcessingTransactions(token = token)
        }.flowOn(Dispatchers.IO).map {

            when (it) {

                is Resource.Success -> {

                    Timber.d("Response -> ${it.value}")

                    appDatabase.processingTransactionsDao()
                        .update(
                            processingTransactions = mapProcessingTransactionsFromResponseToEntity(
                                it.value ?: listOf()
                            )
                        )
                    Resource.Success(Unit)
                }

                is Resource.Failure -> it

                is Resource.Loading -> it

            }

        }

    }

    suspend fun updateCustomerTotalTransactionsDetailsInCache(): Flow<Resource<Unit>> {

        return SafeApiCall.makeNetworkCall {
            val token = sessionManager.fetchAuthToken().getEmptyIfNull()
            apiServices.fetchCustomerTransactionDetails(token = token)
        }.flowOn(Dispatchers.IO).map {

            when (it) {

                is Resource.Success -> {
                    appDatabase.customerTotalTransactionDetailsDao()
                        .update(totalTransactionDetails = mapResponseToEntity(it))
                    Resource.Success(Unit)
                }

                is Resource.Failure -> it

                is Resource.Loading -> it

            }

        }

    }

    private fun mapProcessingTransactionsFromResponseToEntity(it: List<ProcessingTransaction>)

            : List<ProcessingTransactionEntity> {

        val entities = it.map { response ->

            ProcessingTransactionEntity(
                createdAt = response.createdAt,
                params = ProcessingTransactionEntity.Params(
                    amountSpent = response.params?.amountSpent,
                    cashbackAmount = response.params?.cashbackAmount
                ),
                shopName = response.shopName,
                transactionType = response.transactionType
            )

        }

        Timber.d("Mapping response for caching -> $entities")

        return entities

    }

    private fun mapResponseToEntity(it: Resource.Success<CustomerTransactionsResponseV2>) =
        CustomerTotalTransactionDetailsEntityV2(
            results = it.value.results?.map { result ->
                CustomerTotalTransactionDetailsEntityV2.Result(
                    amount = result?.amount,
                    afterAmount = result?.afterAmount,
                    createdAt = result?.createdAt,
                    params = CustomerTotalTransactionDetailsEntityV2.Result.Params(
                        cashbackAmount = result?.params?.cashbackAmount,
                        feeAmount = result?.params?.feeAmount
                    ),
                    shopName = result?.shopName,
                    transactionType = result?.transactionType
                )
            },
            transactionDetails = CustomerTotalTransactionDetailsEntityV2.TransactionDetails(
                cashbackBalance = it.value.transactionDetails?.cashbackBalance,
                processing = it.value.transactionDetails?.processing
            )
        )

    fun getCustomerTotalTransactionDetails(): LiveData<CustomerTotalTransactionDetailsEntityV2?> {
        return appDatabase.customerTotalTransactionDetailsDao().get()
    }

    fun getProcessingTransactionsFromCache(): LiveData<List<ProcessingTransactionEntity>?> {
        return appDatabase.processingTransactionsDao().getAll()
    }

    fun fetchCustomerTransactions(token: String, page: Int, pageSize: Int) =
        apiServices.fetchCustomerTransactions(token = token, page = page, pageSize = pageSize)

    fun createTransaction(token: String, request: CreateTransactionRequest) =
        apiServices.createCustomerTransaction(token = token, request = request)

    suspend fun getProcessingTransactions(): Flow<Resource<List<ProcessingTransaction>?>> {

        val token = sessionManager.fetchAuthToken().getEmptyIfNull()
        return SafeApiCall.makeNetworkCall {
            apiServices.getProcessingTransactions(token = token)
        }.flowOn(Dispatchers.IO)

    }

    suspend fun withdrawAmount(request: HashMap<String, Any>): Flow<Resource<WithdrawAmountResponse>> {

        val token = sessionManager.fetchAuthToken().getEmptyIfNull()
        return SafeApiCall.makeNetworkCall {
            apiServices.withdrawAmount(token = token, request = request)
        }.flowOn(Dispatchers.IO)

    }

    suspend fun payByKash4me(request: PayByKash4meRequest): Flow<Resource<PayByKash4meResponse>> {

        val token = sessionManager.fetchAuthToken().getEmptyIfNull()
        return SafeApiCall.makeNetworkCall {
            apiServices.payByKash4me(token = token, request = request)
        }.flowOn(Dispatchers.IO)

    }

}