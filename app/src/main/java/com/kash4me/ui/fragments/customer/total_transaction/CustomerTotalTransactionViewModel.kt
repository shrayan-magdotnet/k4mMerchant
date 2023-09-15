package com.kash4me.ui.fragments.customer.total_transaction

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.GsonBuilder
import com.kash4me.data.local.customer.total_transactions.CustomerTotalTransactionDetailsEntityV2
import com.kash4me.data.local.customer.total_transactions.ProcessingTransactionEntity
import com.kash4me.data.models.ErrorResponse
import com.kash4me.data.models.customer.create_transaction.CreateTransactionRequest
import com.kash4me.data.models.customer.create_transaction.CreateTransactionResponse
import com.kash4me.data.models.customer.response.CustomerTransactionsResponse
import com.kash4me.data.models.customer.withdraw_amount.WithdrawAmountResponse
import com.kash4me.repository.CustomerTotalCashBackRepository
import com.kash4me.utils.SingleLiveEvent
import com.kash4me.utils.network.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class CustomerTotalTransactionViewModel
@Inject
constructor(private val customerTotalCashBackRepository: CustomerTotalCashBackRepository)

    : ViewModel() {

    val errorMessage = SingleLiveEvent<String>()

    fun updateCustomerTotalTransactionsDetailsInCache(): MutableLiveData<Resource<Unit>> {

        var result = MutableLiveData<Resource<Unit>>()

        viewModelScope.launch {

            result = customerTotalCashBackRepository.updateCustomerTotalTransactionsDetailsInCache()
                .asLiveData() as MutableLiveData<Resource<Unit>>

        }

        return result

    }

    fun updateProcessingTransactionsInCache(): MutableLiveData<Resource<List<Unit>>> {

        var result = MutableLiveData<Resource<List<Unit>>>()

        viewModelScope.launch {

            result = customerTotalCashBackRepository.updateProcessingTransactionsInCache()
                .asLiveData() as MutableLiveData<Resource<List<Unit>>>

        }

        return result

    }

    fun getTotalTransactionDetails(): LiveData<CustomerTotalTransactionDetailsEntityV2?> {

        return customerTotalCashBackRepository.getCustomerTotalTransactionDetails()

    }

    fun getProcessingTransactionsFromCache(): LiveData<List<ProcessingTransactionEntity>?> {

        return customerTotalCashBackRepository.getProcessingTransactionsFromCache()

    }

    fun fetchCustomerTransactions(
        token: String,
        page: Int,
        pageSize: Int
    ): LiveData<CustomerTransactionsResponse> {

        val transactions = MutableLiveData<CustomerTransactionsResponse>()

        val response = customerTotalCashBackRepository.fetchCustomerTransactions(
            token = token, page = page, pageSize = pageSize
        )
        response.enqueue(object : Callback<CustomerTransactionsResponse> {
            override fun onResponse(
                call: Call<CustomerTransactionsResponse>,
                response: Response<CustomerTransactionsResponse>
            ) {

                if (response.isSuccessful) {

                    transactions.postValue(response.body())
                    return

                } else {

                    val gson = GsonBuilder().create()
                    val mError: ErrorResponse

                    try {

                        mError = gson.fromJson(
                            response.errorBody()!!.string(),
                            ErrorResponse::class.java
                        )
                        onFailure(call = call, Throwable(message = mError.error))
//                        errorMessage.postValue(mError.error)

                    } catch (e: Exception) {

                        // handle failure at error parse
                        e.printStackTrace()
                        onFailure(call = call, Throwable(message = e.localizedMessage))

                    }
                }

            }

            override fun onFailure(call: Call<CustomerTransactionsResponse>, t: Throwable) {
                Log.d("TAG", "onFailure: $t")
                errorMessage.postValue(t.message)
            }
        })

        return transactions

    }

    fun createTransaction(
        token: String,
        amountSpent: String,
    ): LiveData<CreateTransactionResponse> {

        val createTransactionResponse = MutableLiveData<CreateTransactionResponse>()

        val request = CreateTransactionRequest(transactionAmount = amountSpent)
        val response = customerTotalCashBackRepository.createTransaction(
            token = token, request = request
        )
        response.enqueue(object : Callback<CreateTransactionResponse> {
            override fun onResponse(
                call: Call<CreateTransactionResponse>,
                response: Response<CreateTransactionResponse>
            ) {

                if (response.isSuccessful) {

                    createTransactionResponse.postValue(response.body())
                    return

                } else {

                    val gson = GsonBuilder().create()
                    val mError: ErrorResponse

                    try {

                        mError = gson.fromJson(
                            response.errorBody()!!.string(),
                            ErrorResponse::class.java
                        )
                        onFailure(call = call, Throwable(message = mError.error))
//                        errorMessage.postValue(mError.error)

                    } catch (e: Exception) {

                        // handle failure at error parse
                        e.printStackTrace()
                        onFailure(call = call, Throwable(message = e.localizedMessage))

                    }
                }

            }

            override fun onFailure(call: Call<CreateTransactionResponse>, t: Throwable) {
                Log.d("TAG", "onFailure: $t")
                errorMessage.postValue(t.message)
            }
        })

        return createTransactionResponse

    }

    fun withdrawAmount(request: HashMap<String, Any>): LiveData<Resource<WithdrawAmountResponse>> {

        var result = MutableLiveData<Resource<WithdrawAmountResponse>>()

        viewModelScope.launch {
            result = customerTotalCashBackRepository.withdrawAmount(request = request)
                .asLiveData() as MutableLiveData<Resource<WithdrawAmountResponse>>
        }

        return result

    }

}