package com.kash4me.ui.activity.merchant.merchant_dashboard

import androidx.lifecycle.ViewModel
import com.kash4me.data.models.merchant.profile.MerchantProfileResponse
import com.kash4me.network.ApiServices
import com.kash4me.network.NetworkConnectionInterceptor
import com.kash4me.network.NotFoundInterceptor
import com.kash4me.repository.UserDetailsRepository
import com.kash4me.utils.App
import com.kash4me.utils.SessionManager
import com.kash4me.utils.extensions.getEmptyIfNull
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class MerchantDashboardViewModel : ViewModel() {

    fun saveMerchantProfileInSession() {

        val sessionManager = SessionManager(App.getContext()!!)
        val apiInterface =
            ApiServices.invoke(
                NetworkConnectionInterceptor(App.getContext()!!),
                NotFoundInterceptor()
            )

        val repository = UserDetailsRepository(apiInterface)

        Timber.d("Fetching merchant details")

        val token = sessionManager.fetchAuthToken().getEmptyIfNull()

        val response = repository.fetchMerchantProfileDetails(token)
        response.enqueue(object : Callback<MerchantProfileResponse> {
            override fun onResponse(
                call: Call<MerchantProfileResponse>,
                response: Response<MerchantProfileResponse>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && response.isSuccessful) {
                        sessionManager.saveMerchantDetails(merchantDetails = responseBody)
                    }
                }

            }

            override fun onFailure(call: Call<MerchantProfileResponse>, t: Throwable) {

            }
        })

    }

}