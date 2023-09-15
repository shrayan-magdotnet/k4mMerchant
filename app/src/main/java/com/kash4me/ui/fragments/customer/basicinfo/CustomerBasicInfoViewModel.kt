package com.kash4me.ui.fragments.customer.basicinfo

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.GsonBuilder
import com.kash4me.data.models.CustomerResponse
import com.kash4me.data.models.ErrorResponse
import com.kash4me.data.models.places_api.ReverseGeocodeResponse
import com.kash4me.data.models.user.CountryResponse
import com.kash4me.repository.PostCustomerDetailsRepository
import com.kash4me.repository.UserRepository
import com.kash4me.repository.google_places.GooglePlacesRepository
import com.kash4me.utils.SingleLiveEvent
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.network.Resource
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class CustomerBasicInfoViewModel(
    private val postCustomerDetailsRepository: PostCustomerDetailsRepository,
    private val userRepository: UserRepository
)

    : ViewModel() {

    var latitude: Double? = null
    var longitude: Double? = null

    val registerResponse = SingleLiveEvent<CustomerResponse>()
    val errorMessage = SingleLiveEvent<String>()

    fun postCustomerDetails(token: String, params: HashMap<String, String>) {

        val response = postCustomerDetailsRepository.postCustomerDetails(token, params)
        response.enqueue(object : Callback<CustomerResponse> {
            override fun onResponse(
                call: Call<CustomerResponse>,
                response: Response<CustomerResponse>
            ) {
                if (response.isSuccessful) {
                    registerResponse.postValue(response.body())
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

            override fun onFailure(call: Call<CustomerResponse>, t: Throwable) {
                Log.d("TAG", "onFailure: $t")
                errorMessage.postValue(t.message)
            }
        })

    }

    fun getAvailableCountries(token: String): LiveData<Resource<CountryResponse>> {

        val result = MutableLiveData<Resource<CountryResponse>>()
        result.value = Resource.Loading

        val response: Call<CountryResponse> =
            userRepository.getAvailableCountries(token = token)
        response.enqueue(object : Callback<CountryResponse> {
            override fun onResponse(
                call: Call<CountryResponse>,
                response: Response<CountryResponse>
            ) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    result.postValue(Resource.Success(responseBody))
                } else {
                    val gson = GsonBuilder().create()
                    val errorResponse: ErrorResponse
                    try {
                        errorResponse = gson.fromJson(
                            response.errorBody()!!.string(),
                            ErrorResponse::class.java
                        )
                        result.postValue(Resource.Failure(errorMsg = errorResponse.error))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        result.postValue(
                            Resource.Failure(
                                errorMsg = e.message.getEmptyIfNull(),
                                exception = e
                            )
                        )
                    }
                }

            }

            override fun onFailure(call: Call<CountryResponse>, t: Throwable) {
                Timber.d("onFailure: $t")
                result.postValue(
                    Resource.Failure(
                        errorMsg = t.message.getEmptyIfNull(),
                        exception = t
                    )
                )
            }
        })

        return result

    }

    fun reverseGeocode(
        country: String,
        postalCode: String
    ): LiveData<Resource<ReverseGeocodeResponse>> {

        var result = MutableLiveData<Resource<ReverseGeocodeResponse>>()

        viewModelScope.launch {
            val googlePlacesRepository = GooglePlacesRepository()
            result = googlePlacesRepository.reverseGeocodeUsingPostalCode(
                country = country, postalCode = postalCode
            ).asLiveData() as MutableLiveData<Resource<ReverseGeocodeResponse>>
        }

        return result

    }

}