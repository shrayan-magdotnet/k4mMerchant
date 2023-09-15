package com.kash4me.ui.fragments.register

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.gson.GsonBuilder
import com.kash4me.data.models.ErrorResponse
import com.kash4me.data.models.SuccessResponse
import com.kash4me.repository.RegisterRepository
import com.kash4me.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RegisterViewModel(private val registerRepository: RegisterRepository) : ViewModel() {

    val registerResponse = SingleLiveEvent<SuccessResponse>()
    val errorMessage = SingleLiveEvent<String>()

    fun registerUser(params: HashMap<String, Any>) {

        val response = registerRepository.registerUser(params)
        response.enqueue(object : Callback<SuccessResponse> {
            override fun onResponse(
                call: Call<SuccessResponse>,
                response: Response<SuccessResponse>
            ) {
                Log.d("TAG", "onResponse: ${response.body()?.detail}")
                if (response.isSuccessful) {
                    registerResponse.postValue(response.body())
                    return


                } else {
//                    val jObject = JSONObject(response.body())
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

            override fun onFailure(call: Call<SuccessResponse>, t: Throwable) {
                Log.d("TAG", "onFailure: $t")
                errorMessage.postValue(t.message)
            }
        })

    }


}
