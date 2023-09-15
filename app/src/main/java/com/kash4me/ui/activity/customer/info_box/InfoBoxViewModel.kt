package com.kash4me.ui.activity.customer.info_box

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.GsonBuilder
import com.kash4me.data.models.ErrorResponse
import com.kash4me.data.models.user.InfoBoxResponse
import com.kash4me.network.ApiServices
import com.kash4me.network.NetworkConnectionInterceptor
import com.kash4me.network.NotFoundInterceptor
import com.kash4me.repository.UserRepository
import com.kash4me.utils.App
import com.kash4me.utils.SessionManager
import com.kash4me.utils.network.Resource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InfoBoxViewModel : ViewModel() {

    private val repository by lazy {
        val apiInterface =
            ApiServices.invoke(
                NetworkConnectionInterceptor(App.getContext()?.applicationContext!!),
                NotFoundInterceptor()
            )
        UserRepository(apiServices = apiInterface)
    }

    fun fetchInfoBox(): MutableLiveData<Resource<InfoBoxResponse>> {

        val infoBoxResponse = MutableLiveData<Resource<InfoBoxResponse>>()

        val token = SessionManager(App.getContext()!!).fetchAuthToken() ?: ""
        val response = repository.getInfoBox(token = token)

        infoBoxResponse.postValue(Resource.Loading)

        response.enqueue(object : Callback<InfoBoxResponse> {
            override fun onResponse(
                call: Call<InfoBoxResponse>,
                response: Response<InfoBoxResponse>
            ) {

                if (response.isSuccessful) {

                    val responseBody = response.body()
                    if (responseBody == null) {

                        infoBoxResponse.postValue(
                            Resource.Failure(errorMsg = "No data found", exception = null)
                        )

                    } else {

                        infoBoxResponse.postValue(Resource.Success(responseBody))

                    }

                } else {

                    val gson = GsonBuilder().create()
                    var errorResponse: ErrorResponse
                    try {
                        errorResponse = gson.fromJson(
                            response.errorBody()!!.string(),
                            ErrorResponse::class.java
                        )
                        Log.d("TAG", "onResponse: ${errorResponse.error}")

                        infoBoxResponse.postValue(Resource.Failure(errorMsg = errorResponse.error))

                    } catch (e: Exception) {
                        e.printStackTrace()
                        errorResponse = ErrorResponse(e.localizedMessage ?: "")
                        Log.d("TAG", "onResponse: ${errorResponse.error}")
                        infoBoxResponse.postValue(
                            Resource.Failure(
                                errorMsg = e.message ?: "Something went wrong",
                                exception = e
                            )
                        )
                    }

                }

            }

            override fun onFailure(call: Call<InfoBoxResponse>, t: Throwable) {
                Log.d("TAG", "onFailure: $t")
                val errorResponse = ErrorResponse(t.localizedMessage ?: "")
                Log.d("TAG", "onResponse: ${errorResponse.error}")
                infoBoxResponse.postValue(
                    Resource.Failure(
                        errorMsg = t.message ?: "Something went wrong",
                        exception = t
                    )
                )
            }

        })

        return infoBoxResponse

    }

}