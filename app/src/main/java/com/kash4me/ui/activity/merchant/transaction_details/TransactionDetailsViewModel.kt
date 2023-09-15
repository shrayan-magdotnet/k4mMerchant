package com.kash4me.ui.activity.merchant.transaction_details

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.gson.GsonBuilder
import com.kash4me.data.models.ErrorResponse
import com.kash4me.data.models.merchant.transaction_by_time.ViewTransactionByTimeResponse
import com.kash4me.repository.MerchantTransactionDetailsRepository
import com.kash4me.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TransactionDetailsViewModel(private val merchantTransactionDetailsRepository: MerchantTransactionDetailsRepository) :
    ViewModel() {

    val viewTransactionByTimeResponse =
        SingleLiveEvent<ViewTransactionByTimeResponse>()
    val errorMessage = SingleLiveEvent<ErrorResponse>()

    fun getMerchantTransactionDetails(
        token: String,
        filterOptions: HashMap<String, Any>
    ) {

        val response = merchantTransactionDetailsRepository.getMerchantTransactionDetails(
            token = token, filterOptions = filterOptions
        )
        response.enqueue(object : Callback<ViewTransactionByTimeResponse> {
            override fun onResponse(
                call: Call<ViewTransactionByTimeResponse>,
                response: Response<ViewTransactionByTimeResponse>
            ) {
                if (response.isSuccessful) {

                    viewTransactionByTimeResponse.postValue(response.body())
                    return

                } else {
                    val gson = GsonBuilder().create()
                    var errorResponse: ErrorResponse
                    try {
                        errorResponse = gson.fromJson(
                            response.errorBody()!!.string(),
                            ErrorResponse::class.java
                        )
                        errorMessage.postValue(errorResponse)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        errorResponse = ErrorResponse(e.localizedMessage ?: "")
                        errorMessage.postValue(errorResponse)
                    }
                }

            }

            override fun onFailure(
                call: Call<ViewTransactionByTimeResponse>,
                t: Throwable
            ) {
                Log.d("TAG", "onFailure: $t")
                val errorResponse = ErrorResponse(t.localizedMessage ?: "")
                errorMessage.postValue(errorResponse)
            }
        })

    }
}
