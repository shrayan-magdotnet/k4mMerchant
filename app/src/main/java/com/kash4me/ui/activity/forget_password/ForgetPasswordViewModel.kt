package com.kash4me.ui.activity.forget_password

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.gson.GsonBuilder
import com.kash4me.data.models.ErrorResponse
import com.kash4me.data.models.SuccessResponse
import com.kash4me.repository.ForgetPasswordRepository
import com.kash4me.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ForgetPasswordViewModel(private val forgetPasswordRepository: ForgetPasswordRepository) :
    ViewModel() {

    val forgetPasswordResponse = SingleLiveEvent<SuccessResponse>()
    val errorMessage = SingleLiveEvent<String>()

    fun forgetPassword(params: HashMap<String, String>) {

        val response = forgetPasswordRepository.forgetPassword(params)
        response.enqueue(object : Callback<SuccessResponse> {
            override fun onResponse(
                call: Call<SuccessResponse>,
                response: Response<SuccessResponse>
            ) {
                if (response.isSuccessful) {
                    forgetPasswordResponse.postValue(response.body())
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
