package com.kash4me.ui.fragments.merchant.search

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.gson.GsonBuilder
import com.kash4me.data.models.CustomerListResponse
import com.kash4me.data.models.ErrorResponse
import com.kash4me.repository.MerchantCustomerListRepository
import com.kash4me.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MerchantSearchViewModel(private val merchantCustomerListRepository: MerchantCustomerListRepository) :
    ViewModel() {

    val customerListResponse = SingleLiveEvent<CustomerListResponse>()
    val errorMessage = SingleLiveEvent<String>()

    var value: String = ""
    var searchQuery: String = ""

    fun getMerchantCustomerList(token: String, filterOptions: Map<String, String>) {

        val response = merchantCustomerListRepository.getMerchantCustomerList(
            token,
            filterOptions
        )
        response.enqueue(object : Callback<CustomerListResponse> {
            override fun onResponse(
                call: Call<CustomerListResponse>,
                response: Response<CustomerListResponse>
            ) {
                if (response.isSuccessful) {
                    customerListResponse.postValue(response.body())
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

            override fun onFailure(call: Call<CustomerListResponse>, t: Throwable) {
                Log.d("TAG", "onFailure: $t")
                errorMessage.postValue(t.message)
            }
        })

    }
}