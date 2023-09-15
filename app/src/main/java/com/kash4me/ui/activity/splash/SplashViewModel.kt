package com.kash4me.ui.activity.splash

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.gson.GsonBuilder
import com.kash4me.data.models.ErrorResponse
import com.kash4me.data.models.MerchantDetailsResponse
import com.kash4me.data.models.customer.customer_details.CustomerDetailsResponse
import com.kash4me.data.models.customer.update_profile.CustomerProfileUpdateRequest
import com.kash4me.data.models.merchant.update_profile.MerchantProfileUpdateRequest
import com.kash4me.data.models.merchant.update_profile.MerchantProfileUpdateResponse
import com.kash4me.repository.UserDetailsRepository
import com.kash4me.utils.App
import com.kash4me.utils.SessionManager
import com.kash4me.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

var customerDetailsResponse: CustomerDetailsResponse? = null

var merchantDetailsResponse: MerchantDetailsResponse? = null


class SplashViewModel(
    private var userDetailsRepository: UserDetailsRepository,
) : ViewModel() {

    val updatedCustomerDetails = SingleLiveEvent<CustomerDetailsResponse>()
    val updatedMerchantDetails = SingleLiveEvent<MerchantDetailsResponse>()
    val errorMessage = SingleLiveEvent<String>()

    private val sessionManager by lazy { SessionManager(context = App.getContext()!!) }

    fun fetchCustomerDetails() {

        val token = sessionManager.fetchAuthToken() ?: ""
        val response = userDetailsRepository.fetchCustomerDetails(token)
        response.enqueue(object : Callback<CustomerDetailsResponse> {
            override fun onResponse(
                call: Call<CustomerDetailsResponse>,
                response: Response<CustomerDetailsResponse>
            ) {
                if (response.isSuccessful) {
                    customerDetailsResponse = response.body()!!
                    sessionManager.saveCustomerDetails(customerDetails = response.body()!!)
                } else {
                    val gson = GsonBuilder().create()
                    var errorResponse: ErrorResponse
                    try {
                        errorResponse = gson.fromJson(
                            response.errorBody()!!.string(),
                            ErrorResponse::class.java
                        )
                        Log.d("TAG", "onResponse: ${errorResponse.error}")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        errorResponse = ErrorResponse(e.localizedMessage ?: "")
                        Log.d("TAG", "onResponse: ${errorResponse.error}")
                    }
                }

            }

            override fun onFailure(call: Call<CustomerDetailsResponse>, t: Throwable) {
                Log.d("TAG", "onFailure: $t")
                val errorResponse = ErrorResponse(t.localizedMessage ?: "")
                Log.d("TAG", "onResponse: ${errorResponse.error}")
            }
        })

    }


    fun updateCustomerDetails(token: String, params: HashMap<String, String>) {

        val response = userDetailsRepository.updateCustomerDetails(token, params)
        Log.d("TAG", "updateCustomerDetails: response: $response")
        response.enqueue(object : Callback<CustomerDetailsResponse> {
            override fun onResponse(
                call: Call<CustomerDetailsResponse>,
                response: Response<CustomerDetailsResponse>
            ) {
                if (response.isSuccessful) {
                    customerDetailsResponse = response.body()!!
                    updatedCustomerDetails.postValue(response.body())
                    sessionManager.saveCustomerDetails(customerDetails = response.body()!!)
                } else {
                    val gson = GsonBuilder().create()
                    val errorResponse: ErrorResponse
                    try {
                        errorResponse = gson.fromJson(
                            response.errorBody()!!.string(),
                            ErrorResponse::class.java
                        )
                        onFailure(call = call, Throwable(message = errorResponse.error))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        onFailure(call = call, Throwable(message = e.localizedMessage))
                    }
                }

            }

            override fun onFailure(call: Call<CustomerDetailsResponse>, t: Throwable) {
                Log.d("TAG", "onFailure: $t")
                errorMessage.postValue(t.message)
            }
        })

    }


    fun updateCustomerDetails(token: String, request: CustomerProfileUpdateRequest) {

        val response = userDetailsRepository.updateCustomerDetails(token, request)
        Log.d("TAG", "updateCustomerDetails: response: $response")
        response.enqueue(object : Callback<CustomerDetailsResponse> {
            override fun onResponse(
                call: Call<CustomerDetailsResponse>,
                response: Response<CustomerDetailsResponse>
            ) {
                if (response.isSuccessful) {
                    customerDetailsResponse = response.body()!!
                    updatedCustomerDetails.postValue(response.body())
                    sessionManager.saveCustomerDetails(customerDetails = response.body()!!)
                } else {
                    val gson = GsonBuilder().create()
                    val errorResponse: ErrorResponse
                    try {
                        errorResponse = gson.fromJson(
                            response.errorBody()!!.string(),
                            ErrorResponse::class.java
                        )
                        onFailure(call = call, Throwable(message = errorResponse.error))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        onFailure(call = call, Throwable(message = e.localizedMessage))
                    }
                }

            }

            override fun onFailure(call: Call<CustomerDetailsResponse>, t: Throwable) {
                Log.d("TAG", "onFailure: $t")
                errorMessage.postValue(t.message)
            }
        })

    }

    fun fetchMerchantDetails(token: String) {

        Log.d("SplashViewModel", "Fetching merchant details")

        val response = userDetailsRepository.fetchMerchantDetails(token)
        response.enqueue(object : Callback<MerchantDetailsResponse> {
            override fun onResponse(
                call: Call<MerchantDetailsResponse>,
                response: Response<MerchantDetailsResponse>
            ) {
                if (response.isSuccessful) {
                    merchantDetailsResponse = response.body()!!
                } else {
                    val gson = GsonBuilder().create()
                    var errorResponse: ErrorResponse
                    try {
                        errorResponse = gson.fromJson(
                            response.errorBody()!!.string(),
                            ErrorResponse::class.java
                        )
                        Log.d("TAG", "onResponse: ${errorResponse.error}")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        errorResponse = ErrorResponse(e.localizedMessage ?: "")
                        Log.d("TAG", "onResponse: ${errorResponse.error}")
                    }
                }

            }

            override fun onFailure(call: Call<MerchantDetailsResponse>, t: Throwable) {
                Log.d("TAG", "onFailure: $t")
                val errorResponse = ErrorResponse(t.localizedMessage ?: "")
                Log.d("TAG", "onResponse: ${errorResponse.error}")
            }
        })

    }

    fun updateMerchantDetails(token: String, params: HashMap<String, Any>, merchantShopID: Int) {

        val response = userDetailsRepository.updateMerchantDetails(token, params, merchantShopID)
        response.enqueue(object : Callback<MerchantDetailsResponse> {
            override fun onResponse(
                call: Call<MerchantDetailsResponse>,
                response: Response<MerchantDetailsResponse>
            ) {
                if (response.isSuccessful) {
                    merchantDetailsResponse = response.body()!!
                    updatedMerchantDetails.postValue(response.body())
                } else {
                    val gson = GsonBuilder().create()
                    val errorResponse: ErrorResponse
                    try {
                        errorResponse = gson.fromJson(
                            response.errorBody()!!.string(),
                            ErrorResponse::class.java
                        )
                        onFailure(call = call, Throwable(message = errorResponse.error))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        onFailure(call = call, Throwable(message = e.localizedMessage))
                    }
                }

            }

            override fun onFailure(call: Call<MerchantDetailsResponse>, t: Throwable) {
                Log.d("TAG", "onFailure: $t")
                errorMessage.postValue(t.message)
            }
        })

    }

    fun updateMerchantDetails(
        token: String,
        request: MerchantProfileUpdateRequest,
        merchantShopID: Int
    ) {

        val response = userDetailsRepository.updateMerchantDetails(token, request, merchantShopID)
        response.enqueue(object : Callback<MerchantProfileUpdateResponse> {
            override fun onResponse(
                call: Call<MerchantProfileUpdateResponse>,
                response: Response<MerchantProfileUpdateResponse>
            ) {
                if (response.isSuccessful) {
//                    merchantDetailsResponse = response.body()!!
//                    updatedMerchantDetails.postValue(response.body())
                } else {
                    val gson = GsonBuilder().create()
                    val errorResponse: ErrorResponse
                    try {
                        errorResponse = gson.fromJson(
                            response.errorBody()!!.string(),
                            ErrorResponse::class.java
                        )
                        onFailure(call = call, Throwable(message = errorResponse.error))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        onFailure(call = call, Throwable(message = e.localizedMessage))
                    }
                }

            }

            override fun onFailure(call: Call<MerchantProfileUpdateResponse>, t: Throwable) {
                Log.d("TAG", "onFailure: $t")
                errorMessage.postValue(t.message)
            }
        })

    }

}
