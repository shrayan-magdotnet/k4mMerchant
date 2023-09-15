package com.kash4me.ui.fragments.customer.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.GsonBuilder
import com.kash4me.data.models.ErrorResponse
import com.kash4me.data.models.customer.customer_details.CustomerDetailsResponse
import com.kash4me.data.models.places_api.ReverseGeocodeResponse
import com.kash4me.data.models.user.CountryResponse
import com.kash4me.repository.UserDetailsRepository
import com.kash4me.repository.UserRepository
import com.kash4me.repository.google_places.GooglePlacesRepository
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.network.Resource
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class CustomerProfileViewModel(
    private val userDetailsRepository: UserDetailsRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    fun fetchCustomerDetails(token: String): LiveData<Resource<CustomerDetailsResponse>> {

        Timber.d("Fetching merchant details")
        val result = MutableLiveData<Resource<CustomerDetailsResponse>>()
        result.value = Resource.Loading

        val response = userDetailsRepository.fetchCustomerDetails(token)
        response.enqueue(object : Callback<CustomerDetailsResponse> {
            override fun onResponse(
                call: Call<CustomerDetailsResponse>,
                response: Response<CustomerDetailsResponse>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && response.isSuccessful) {
                        result.value = Resource.Success(responseBody)
                    } else {
                        result.value = Resource.Failure(errorMsg = response.message())
                    }
                } else {
                    val gson = GsonBuilder().create()
                    var errorResponse: ErrorResponse
                    try {
                        errorResponse = gson.fromJson(
                            response.errorBody()!!.string(),
                            ErrorResponse::class.java
                        )
                        Timber.d("onResponse: ${errorResponse.error}")
                        result.value = Resource.Failure(errorMsg = errorResponse.error)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        errorResponse = ErrorResponse(e.localizedMessage ?: "")
                        Timber.d("onResponse: ${errorResponse.error}")
                        result.value =
                            Resource.Failure(errorMsg = e.message.getEmptyIfNull(), exception = e)
                    }
                }

            }

            override fun onFailure(call: Call<CustomerDetailsResponse>, t: Throwable) {
                Timber.d("onFailure: $t")
                val errorResponse = ErrorResponse(t.localizedMessage ?: "")
                Timber.d("onResponse: ${errorResponse.error}")
                result.value =
                    Resource.Failure(errorMsg = t.message.getEmptyIfNull(), exception = t)
            }
        })

        return result

    }

    fun updateCustomerDetails(
        token: String,
        request: HashMap<String, Any?>
    ): LiveData<Resource<CustomerDetailsResponse?>> {

        var result = MutableLiveData<Resource<CustomerDetailsResponse?>>()

        viewModelScope.launch {
            result = userDetailsRepository.updateCustomerDetails(token = token, params = request)
                .asLiveData() as MutableLiveData<Resource<CustomerDetailsResponse?>>
        }

        return result

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