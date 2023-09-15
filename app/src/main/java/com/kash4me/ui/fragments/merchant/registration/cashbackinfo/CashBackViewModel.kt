package com.kash4me.ui.fragments.merchant.registration.cashbackinfo

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.gson.GsonBuilder
import com.kash4me.data.models.ActiveCashbackSettings
import com.kash4me.data.models.ErrorResponse
import com.kash4me.data.models.merchant.cashback.AddCashbackRequest
import com.kash4me.data.models.merchant.cashback.CashbackResponseV2
import com.kash4me.repository.CashBackRepository
import com.kash4me.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CashBackViewModel(private val cashBackRepository: CashBackRepository) : ViewModel() {

    var addCashbackRequest: AddCashbackRequest? = null


    val cashBackResponse = SingleLiveEvent<CashbackResponseV2>()
    val activeCashbackSettings = SingleLiveEvent<ActiveCashbackSettings>()
    val errorMessage = SingleLiveEvent<String>()


    fun addCashBack(
        merchantShopID: Int,
        token: String,
        userParams: HashMap<String, Any>,
    ) {

        val response = cashBackRepository.addCashBack(merchantShopID, token, userParams)
        response.enqueue(object : Callback<CashbackResponseV2> {
            override fun onResponse(
                call: Call<CashbackResponseV2>,
                response: Response<CashbackResponseV2>
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

            override fun onFailure(call: Call<CashbackResponseV2>, t: Throwable) {
                Log.d("TAG", "onFailure: $t")
                errorMessage.postValue(t.message)
            }
        })

    }

    fun getActiveCashBack(
        merchantShopID: Int,
        token: String,
    ) {

        val response = cashBackRepository.getActiveCashBackSetting(merchantShopID, token)
        response.enqueue(object : Callback<ActiveCashbackSettings> {
            override fun onResponse(
                call: Call<ActiveCashbackSettings>,
                response: Response<ActiveCashbackSettings>
            ) {
                if (response.isSuccessful) {
                    activeCashbackSettings.postValue(response.body())

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

            override fun onFailure(call: Call<ActiveCashbackSettings>, t: Throwable) {
                Log.d("TAG", "onFailure: $t")
                errorMessage.postValue(t.message)
            }
        })


    }
}