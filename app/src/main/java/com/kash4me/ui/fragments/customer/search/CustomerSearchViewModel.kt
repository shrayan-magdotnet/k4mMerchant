package com.kash4me.ui.fragments.customer.search

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.gson.GsonBuilder
import com.kash4me.data.models.ErrorResponse
import com.kash4me.data.models.NearByMerchantsResponse
import com.kash4me.repository.MerchantsRepository
import com.kash4me.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CustomerSearchViewModel(private val merchantsRepository: MerchantsRepository) :
    ViewModel() {

    var orderBy: String = ""
    var searchQuery: String = ""

    var latitude: String = ""
    var longitude: String = ""

    val nearByMerchantResponse = SingleLiveEvent<NearByMerchantsResponse>()
    val errorMessage = SingleLiveEvent<String>()

    fun getMerchants(token: String, filterOptions: Map<String, String>) {

        val response = merchantsRepository.getMerchants(token, filterOptions = filterOptions)
        response.enqueue(object : Callback<NearByMerchantsResponse> {
            override fun onResponse(
                call: Call<NearByMerchantsResponse>,
                response: Response<NearByMerchantsResponse>
            ) {
                if (response.isSuccessful) {
                    nearByMerchantResponse.postValue(response.body())
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

            override fun onFailure(call: Call<NearByMerchantsResponse>, t: Throwable) {
                Log.d("TAG", "onFailure: $t")
                errorMessage.postValue(t.message)
            }
        })

    }
}