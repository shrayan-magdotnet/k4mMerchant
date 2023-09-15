package com.kash4me.ui.fragments.merchant.registration.basicinfo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.GsonBuilder
import com.kash4me.data.models.BusinessInfoResponse
import com.kash4me.data.models.ErrorResponse
import com.kash4me.data.models.merchant.profile.MerchantProfileResponse
import com.kash4me.data.models.merchant.update_profile.MerchantProfileUpdateResponse
import com.kash4me.data.models.places_api.ReverseGeocodeResponse
import com.kash4me.data.models.places_api.TimezoneResponse
import com.kash4me.data.models.user.CountryResponse
import com.kash4me.data.models.user.TagResponse
import com.kash4me.repository.PostMerchantDetailsRepository
import com.kash4me.repository.UserDetailsRepository
import com.kash4me.repository.UserRepository
import com.kash4me.repository.google_places.GooglePlacesRepository
import com.kash4me.utils.SingleLiveEvent
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.network.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MerchantBasicInfoViewModel @Inject constructor(
    private val repository: PostMerchantDetailsRepository,
    private val userRepository: UserRepository,
    private val userDetailsRepository: UserDetailsRepository,
)

    : ViewModel() {

    var logoFile: File? = null
    var coverPhotoFile: File? = null

    var selectedCountry: CountryResponse.Country? = null
    var selectedTimezone: String? = null

    var latitude: Double? = null
    var longitude: Double? = null

    val merchantRegistrationResponse = SingleLiveEvent<BusinessInfoResponse>()
    val errorMessage = SingleLiveEvent<String>()

    var shouldUpdateMerchantDetails = false

    fun postBusinessUserDetails(
        token: String,
        userParams: HashMap<String, Any>,
    ): MutableLiveData<Resource<BusinessInfoResponse>> {

        var result = MutableLiveData<Resource<BusinessInfoResponse>>()

        viewModelScope.launch {

            result = repository.postBusinessUserDetails(token, userParams)
                .asLiveData() as MutableLiveData<Resource<BusinessInfoResponse>>

        }

        return result


//        val response =
//            response.enqueue(object : Callback<BusinessInfoResponse> {
//                override fun onResponse(
//                    call: Call<BusinessInfoResponse>,
//                    response: Response<BusinessInfoResponse>
//                ) {
//                    if (response.isSuccessful) {
//                        merchantRegistrationResponse.postValue(response.body())
//
//                        return
//                    } else {
//                        val gson = GsonBuilder().create()
//                        val mError: ErrorResponse
//                        try {
//                            mError = gson.fromJson(
//                                response.errorBody()!!.string(),
//                                ErrorResponse::class.java
//                            )
//                            onFailure(call = call, Throwable(message = mError.error))
//                        } catch (e: Exception) {
//                            e.printStackTrace()
//                            onFailure(call = call, Throwable(message = e.localizedMessage))
//                        }
//                    }
//
//                }
//
//                override fun onFailure(call: Call<BusinessInfoResponse>, t: Throwable) {
//                    Log.d("TAG", "onFailure: $t")
//                    errorMessage.postValue(t.message)
//                }
//            })

    }

//    fun postBusinessDetails(
//        token: String,
//        params: HashMap<String, String>,
//    ) {
//
//        val response = postMerchantDetailsRepository.postBusinessDetails(token, params)
//        response.enqueue(object : Callback<MerchantResponse> {
//            override fun onResponse(
//                call: Call<MerchantResponse>,
//                response: Response<MerchantResponse>
//            ) {
//                if (response.isSuccessful) {
//                    merchantResponse.postValue(response.body())
//
//                    return
//                } else {
//                    val gson = GsonBuilder().create()
//                    val mError: ErrorResponse
//                    try {
//                        mError = gson.fromJson(
//                            response.errorBody()!!.string(),
//                            ErrorResponse::class.java
//                        )
//                        onFailure(call = call, Throwable(message = mError.error))
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                        onFailure(call = call, Throwable(message = e.localizedMessage))
//                    }
//                }
//
//            }
//
//            override fun onFailure(call: Call<MerchantResponse>, t: Throwable) {
//                Log.d("TAG", "onFailure: $t")
//                errorMessage.postValue(t.message)
//            }
//        })
//
//    }

    fun updateMerchantDetails(
        token: String,
        params: HashMap<String, Any?>,
        merchantShopID: Int
    ): SingleLiveEvent<MerchantProfileUpdateResponse> {

        val merchantUpdateResponse = SingleLiveEvent<MerchantProfileUpdateResponse>()

        val response = repository.updateMerchantDetails(token, params, merchantShopID)
        response.enqueue(object : Callback<MerchantProfileUpdateResponse> {
            override fun onResponse(
                call: Call<MerchantProfileUpdateResponse>,
                response: Response<MerchantProfileUpdateResponse>
            ) {

                Timber.d("onResponse")
                if (response.isSuccessful) {
//                    merchantDetailsResponse = response.body()!!
                    merchantUpdateResponse.postValue(response.body())
                } else {
                    val gson = GsonBuilder().create()
                    val errorResponse: ErrorResponse
                    try {
                        errorResponse = gson.fromJson(
                            response.errorBody()!!.string(),
                            ErrorResponse::class.java
                        )
                        Timber.d("Failure in try")
                        onFailure(call = call, Throwable(message = errorResponse.error))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Timber.d("Exception caught: ${e.localizedMessage}")
                        onFailure(call = call, Throwable(message = e.localizedMessage))
                    }
                }

            }

            override fun onFailure(call: Call<MerchantProfileUpdateResponse>, t: Throwable) {
                Timber.d("onFailure: ${t.message}")
                errorMessage.postValue(t.message)
            }
        })

        return merchantUpdateResponse

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

    fun getAvailableTags(): LiveData<Resource<TagResponse>> {

        var result = MutableLiveData<Resource<TagResponse>>()

        viewModelScope.launch {
            result = userRepository.getAvailableTags()
                .asLiveData() as MutableLiveData<Resource<TagResponse>>
        }

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

    fun getTimezone(
        latitude: Double, longitude: Double
    ): LiveData<Resource<TimezoneResponse>> {

        var result = MutableLiveData<Resource<TimezoneResponse>>()

        viewModelScope.launch {
            val googlePlacesRepository = GooglePlacesRepository()
            result = googlePlacesRepository.getTimezone(latitude = latitude, longitude = longitude)
                .asLiveData() as MutableLiveData<Resource<TimezoneResponse>>
        }

        return result

    }

    fun updateMerchantLogo(token: String, merchantShopId: Int, logo: File)

            : LiveData<Resource<MerchantProfileResponse>> {

        var result = MutableLiveData<Resource<MerchantProfileResponse>>()

        viewModelScope.launch {

            val requestFile = logo.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("logo", logo.name, requestFile)

            result = userDetailsRepository.updateMerchantProfileLogo(
                token = token, merchantShopID = merchantShopId, imagePart = imagePart

            ).asLiveData() as MutableLiveData<Resource<MerchantProfileResponse>>
        }

        return result

    }

//    fun getTimezones(countryCode: String): LiveData<Resource<TimezoneResponse>> {
//
//        var result = MutableLiveData<Resource<TimezoneResponse>>()
//
//        viewModelScope.launch {
//            result = userRepository.getTimezones(countryCode = countryCode)
//                .asLiveData() as MutableLiveData<Resource<TimezoneResponse>>
//        }
//
//        return result
//
//    }

}