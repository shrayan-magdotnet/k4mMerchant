package com.kash4me.ui.fragments.merchant.search.assign_cashback.fragment.customer_details

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.GsonBuilder
import com.kash4me.data.models.ErrorResponse
import com.kash4me.data.models.merchant.CustomerDetailsResponseDto
import com.kash4me.data.models.merchant.rollback_transaction.RollbackTransactionRequest
import com.kash4me.data.models.merchant.rollback_transaction.RollbackTransactionResponse
import com.kash4me.repository.CustomerDetailsFromMerchantRepository
import com.kash4me.utils.SingleLiveEvent
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.network.Resource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class CustomerDetailsFromMerchantViewModel(private val customerDetailsFromMerchantRepository: CustomerDetailsFromMerchantRepository) :
    ViewModel() {
    val customerDetailsFromMerchantResponse = SingleLiveEvent<CustomerDetailsResponseDto>()
    val errorMessage = SingleLiveEvent<String>()

    fun getCustomerDetailsFromMerchant(token: String, customerId: Int) {

        val response = customerDetailsFromMerchantRepository.getCustomerDetailsFromMerchant(
            token,
            customerId
        )
        response.enqueue(object : Callback<CustomerDetailsResponseDto> {
            override fun onResponse(
                call: Call<CustomerDetailsResponseDto>,
                response: Response<CustomerDetailsResponseDto>
            ) {
                if (response.isSuccessful) {
                    customerDetailsFromMerchantResponse.postValue(response.body())
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

            override fun onFailure(call: Call<CustomerDetailsResponseDto>, t: Throwable) {
                Log.d("TAG", "onFailure: $t")
                errorMessage.postValue(t.message)
            }
        })

    }

    fun rollbackTransaction(token: String, amountSpent: String, customerId: Int, merchantId: Int)

            : LiveData<Resource<RollbackTransactionResponse>> {

        val result = MutableLiveData<Resource<RollbackTransactionResponse>>()
        result.value = Resource.Loading

        val request = RollbackTransactionRequest(
            amountSpent = amountSpent, customerId = customerId, merchantId = merchantId
        )

        val response = customerDetailsFromMerchantRepository.rollbackTransaction(
            token = token, request = request
        )
        response.enqueue(object : Callback<RollbackTransactionResponse> {
            override fun onResponse(
                call: Call<RollbackTransactionResponse>,
                response: Response<RollbackTransactionResponse>
            ) {

                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {

                    result.postValue(Resource.Success(responseBody))
                    return

                } else {

                    val gson = GsonBuilder().create()
                    val mError: ErrorResponse
                    try {
                        mError = gson.fromJson(
                            response.errorBody()!!.string(),
                            ErrorResponse::class.java
                        )
                        result.postValue(Resource.Failure(errorMsg = mError.error))

                    } catch (e: Exception) {
                        // handle failure at error parse
                        e.printStackTrace()
                        result.postValue(
                            Resource.Failure(
                                errorMsg = e.localizedMessage.getEmptyIfNull(), exception = e
                            )
                        )
                    }
                }

            }

            override fun onFailure(call: Call<RollbackTransactionResponse>, t: Throwable) {
                Timber.d("onFailure: $t")
                result.postValue(
                    Resource.Failure(errorMsg = t.message.getEmptyIfNull(), exception = t)
                )
            }
        })

        return result

    }
}