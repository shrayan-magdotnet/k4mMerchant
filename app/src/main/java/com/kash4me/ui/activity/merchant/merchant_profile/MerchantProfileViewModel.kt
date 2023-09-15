package com.kash4me.ui.activity.merchant.merchant_profile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.GsonBuilder
import com.kash4me.data.models.ErrorResponse
import com.kash4me.data.models.merchant.profile.MerchantProfileResponse
import com.kash4me.data.models.places_api.ReverseGeocodeResponse
import com.kash4me.data.models.user.CountryResponse
import com.kash4me.data.models.user.TagResponse
import com.kash4me.repository.UserDetailsRepository
import com.kash4me.repository.UserRepository
import com.kash4me.repository.google_places.GooglePlacesRepository
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
class MerchantProfileViewModel @Inject constructor(
    private val userDetailsRepository: UserDetailsRepository,
    private val userRepository: UserRepository
)

    : ViewModel() {

    var logoFile: File? = null
    val hasLogoBeenUpdated = MutableLiveData<Boolean>().apply { value = false }

    var coverPhotoFile: File? = null
    val hasCoverPhotoBeenUpdated = MutableLiveData<Boolean>().apply { value = false }

    var logoUri: Uri? = null
    var logoPath: String? = null

    fun fetchMerchantDetails(token: String): LiveData<Resource<MerchantProfileResponse>> {

        Timber.d("Fetching merchant details")
        val result = MutableLiveData<Resource<MerchantProfileResponse>>()
        result.value = Resource.Loading

        val response = userDetailsRepository.fetchMerchantProfileDetails(token)
        response.enqueue(object : Callback<MerchantProfileResponse> {
            override fun onResponse(
                call: Call<MerchantProfileResponse>,
                response: Response<MerchantProfileResponse>
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

            override fun onFailure(call: Call<MerchantProfileResponse>, t: Throwable) {
                Timber.d("onFailure: $t")
                val errorResponse = ErrorResponse(t.localizedMessage ?: "")
                Timber.d("onResponse: ${errorResponse.error}")
                result.value =
                    Resource.Failure(errorMsg = t.message.getEmptyIfNull(), exception = t)
            }
        })

        return result

    }

    fun updateMerchantDetails(
        token: String,
        params: HashMap<String, Any?>,
        merchantShopID: Int,
    ): LiveData<Resource<MerchantProfileResponse>> {

        val result = MutableLiveData<Resource<MerchantProfileResponse>>()
        result.value = Resource.Loading

        val response = userDetailsRepository.updateMerchantProfileDetails(
            token = token, merchantShopID = merchantShopID, params = params
        )
        response.enqueue(object : Callback<MerchantProfileResponse> {
            override fun onResponse(
                call: Call<MerchantProfileResponse>,
                response: Response<MerchantProfileResponse>
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
                            Resource.Failure(errorMsg = e.message.getEmptyIfNull(), exception = e)
                        )
                    }
                }

            }

            override fun onFailure(call: Call<MerchantProfileResponse>, t: Throwable) {
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

}