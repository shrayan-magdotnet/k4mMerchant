package com.kash4me.ui.activity.customer.merchant_details

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.gson.GsonBuilder
import com.kash4me.data.models.ErrorResponse
import com.kash4me.data.models.customer.view_merchant_details.MerchantDetailsResponse
import com.kash4me.repository.AnnouncementRepository
import com.kash4me.repository.MerchantDetailsWithCustomerInfoRepository
import com.kash4me.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class MerchantDetailsViewModel @Inject constructor(
    val repository: MerchantDetailsWithCustomerInfoRepository,
    val announcementRepository: AnnouncementRepository
)

    : ViewModel() {

    val merchantDetailsResponse = SingleLiveEvent<MerchantDetailsResponse>()
    val errorMessage = SingleLiveEvent<ErrorResponse>()

    fun getMerchantDetailsWithCustomerInfo(
        merchantId: Int,
        token: String,
        lat: Double,
        lng: Double
    ) {

        val response = repository.getMerchantDetailsWithCustomerInfo(
            merchantId,
            token,
            lat,
            lng
        )
        response.enqueue(object : Callback<MerchantDetailsResponse> {
            override fun onResponse(
                call: Call<MerchantDetailsResponse>,
                response: Response<MerchantDetailsResponse>
            ) {
                if (response.isSuccessful) {
                    merchantDetailsResponse.postValue(response.body())
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
                call: Call<MerchantDetailsResponse>,
                t: Throwable
            ) {
                Log.d("TAG", "onFailure: $t")
                val errorResponse = ErrorResponse(t.localizedMessage ?: "")
                errorMessage.postValue(errorResponse)
            }
        })

    }
}
