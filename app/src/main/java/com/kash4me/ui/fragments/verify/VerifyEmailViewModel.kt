package com.kash4me.ui.fragments.verify

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.gson.GsonBuilder
import com.kash4me.data.models.ErrorResponse
import com.kash4me.data.models.SuccessResponse
import com.kash4me.data.models.user.VerifyEmailResponse
import com.kash4me.repository.VerifyEmailRepository
import com.kash4me.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class VerifyEmailViewModel(private val verifyEmailRepository: VerifyEmailRepository) : ViewModel() {

    val resendOTPResponse = SingleLiveEvent<SuccessResponse>()

    val loginResponse = SingleLiveEvent<VerifyEmailResponse>()
    val errorMessage = SingleLiveEvent<String>()

    fun verifyEmail(params: HashMap<String, String>) {

        val response = verifyEmailRepository.verifyEmail(params)
        response.enqueue(object : Callback<VerifyEmailResponse> {
            override fun onResponse(
                call: Call<VerifyEmailResponse>,
                response: Response<VerifyEmailResponse>
            ) {
                if (response.isSuccessful) {
                    Timber.d("Response -> ${response.body()}")
                    loginResponse.postValue(response.body())
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

            override fun onFailure(call: Call<VerifyEmailResponse>, t: Throwable) {
                Log.d("TAG", "onFailure: $t")
                errorMessage.postValue(t.message)
            }
        })

    }

    fun resendOTP(params: HashMap<String, String>) {

        val response = verifyEmailRepository.resendOTP(params)
        response.enqueue(object : Callback<SuccessResponse> {
            override fun onResponse(
                call: Call<SuccessResponse>,
                response: Response<SuccessResponse>
            ) {
                if (response.isSuccessful) {
                    resendOTPResponse.postValue(response.body())
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

            override fun onFailure(call: Call<SuccessResponse>, t: Throwable) {
                Log.d("TAG", "onFailure: $t")
                errorMessage.postValue(t.message)
            }
        })


    }

}