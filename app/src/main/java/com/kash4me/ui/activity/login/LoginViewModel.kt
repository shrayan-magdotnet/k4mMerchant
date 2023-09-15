package com.kash4me.ui.activity.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.GsonBuilder
import com.kash4me.data.models.ErrorResponse
import com.kash4me.data.models.Merchant
import com.kash4me.data.models.user.LoginResponse
import com.kash4me.repository.LoginRepository
import com.kash4me.repository.MerchantTransactionSummaryRepository
import com.kash4me.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.ConnectException
import javax.inject.Inject

@HiltViewModel
class LoginViewModel
@Inject
constructor(
    private val loginRepository: LoginRepository,
    val merchantTransactionSummaryRepository: MerchantTransactionSummaryRepository
) : ViewModel() {

    val merchantResponse = SingleLiveEvent<Merchant>()
    val loginResponse = SingleLiveEvent<LoginResponse>()
    val errorMessage = SingleLiveEvent<ErrorResponse>()

    fun loginCustomer(request: HashMap<String, Any>) {

        viewModelScope.launch { merchantTransactionSummaryRepository.clearTransactionSummaryFromDb() }

        val response = loginRepository.loginCustomer(request = request)
        response.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                if (response.isSuccessful) {
                    loginResponse.postValue(response.body())

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

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.d("TAG", "onFailure: $t")
                val message = if (t is ConnectException) {
                    "Something went wrong"
                } else {
                    t.localizedMessage ?: "Something went wrong"
                }
                val errorResponse = ErrorResponse(message)
                errorMessage.postValue(errorResponse)
            }
        })

    }

    fun loginMerchant(request: HashMap<String, Any>) {

        val response = loginRepository.loginMerchant(request = request)
        response.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                if (response.isSuccessful) {
                    loginResponse.postValue(response.body())
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

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.d("TAG", "onFailure: $t")
                val message = if (t is ConnectException) {
                    "Something went wrong"
                } else {
                    t.localizedMessage ?: "Something went wrong"
                }
                val errorResponse = ErrorResponse(message)
                errorMessage.postValue(errorResponse)
            }
        })

    }

    fun fetchMerchantDetailsForCashBack(
        token: String
    ) {
        val response = loginRepository.fetchMerchantDetailsForCashBack(token)
        response.enqueue(object : Callback<Merchant> {
            override fun onResponse(
                call: Call<Merchant>,
                response: Response<Merchant>
            ) {
                if (response.isSuccessful) {
                    merchantResponse.postValue(response.body())
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

            override fun onFailure(call: Call<Merchant>, t: Throwable) {
                Log.d("TAG", "onFailure: $t")
                val message = if (t is ConnectException) {
                    "Something went wrong"
                } else {
                    t.localizedMessage ?: "Something went wrong"
                }
                val errorResponse = ErrorResponse(message)
                errorMessage.postValue(errorResponse)
            }
        })

    }


}
