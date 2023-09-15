package com.kash4me.ui.activity.calculate_cashback

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.gson.GsonBuilder
import com.kash4me.data.models.CashBackAmountResponse
import com.kash4me.data.models.CashBackSuccessResponse
import com.kash4me.data.models.ErrorResponse
import com.kash4me.repository.CashBackRepository
import com.kash4me.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class CalculateCashBackViewModel(private val cashBackRepository: CashBackRepository) : ViewModel() {
    val cashBackSuccessResponse = SingleLiveEvent<CashBackSuccessResponse>()
    val cashBackResponse = SingleLiveEvent<CashBackAmountResponse>()
    val errorMessage = SingleLiveEvent<String>()


    fun getCashBackInfo(
        token: String,
        userParams: HashMap<String, Any>,
    ) {

        Timber.d("Let's hit API")
        val response = cashBackRepository.getCashbackValue(token, userParams)
        response.enqueue(object : Callback<CashBackAmountResponse> {
            override fun onResponse(
                call: Call<CashBackAmountResponse>,
                response: Response<CashBackAmountResponse>
            ) {
                if (response.isSuccessful) {
                    cashBackResponse.postValue(response.body())

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
                    } catch (e: Exception) {
                        e.printStackTrace()
                        onFailure(call = call, Throwable(message = e.localizedMessage))
                    }
                }

            }

            override fun onFailure(call: Call<CashBackAmountResponse>, t: Throwable) {
                Log.d("TAG", "onFailure: $t")
                errorMessage.postValue(t.message)
            }
        })

    }


    fun createCashBackTransaction(
        merchantShopID: Int,
        token: String,
        userParams: HashMap<String, Any>,
    ) {

        val response =
            cashBackRepository.createCashBackTransaction(merchantShopID, token, userParams)
        response.enqueue(object : Callback<CashBackSuccessResponse> {
            override fun onResponse(
                call: Call<CashBackSuccessResponse>,
                response: Response<CashBackSuccessResponse>
            ) {
                if (response.isSuccessful) {
                    cashBackSuccessResponse.postValue(response.body())

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
                    } catch (e: Exception) {
                        e.printStackTrace()
                        onFailure(call = call, Throwable(message = e.localizedMessage))
                    }
                }

            }

            override fun onFailure(call: Call<CashBackSuccessResponse>, t: Throwable) {
                Log.d("TAG", "onFailure: $t")
                errorMessage.postValue(t.message)
            }
        })

    }
}