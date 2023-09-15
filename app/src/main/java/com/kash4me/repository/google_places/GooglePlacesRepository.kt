package com.kash4me.repository.google_places

import com.kash4me.R
import com.kash4me.data.models.places_api.DetectCountryResponse
import com.kash4me.data.models.places_api.ReverseGeocodeResponse
import com.kash4me.data.models.places_api.TimezoneResponse
import com.kash4me.network.EndPoint
import com.kash4me.network.GooglePlacesApi
import com.kash4me.utils.App
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.network.Resource
import com.kash4me.utils.network.SafeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class GooglePlacesRepository {

    suspend fun reverseGeocodeUsingPostalCode(
        country: String,
        postalCode: String
    ): Flow<Resource<ReverseGeocodeResponse>> {

        val googlePlacesApi = getGooglePlacesApi()
        val apiKey = App.getContext()?.resources?.getString(R.string.places_api_key)
            .getEmptyIfNull()
        val url = buildUrl(country = country, postalCode = postalCode, placesApiKey = apiKey)
        return SafeApiCall.makeNetworkCall { googlePlacesApi.reverseGeocode(url = url) }
            .flowOn(Dispatchers.IO)

    }

    suspend fun getTimezone(
        latitude: Double,
        longitude: Double
    ): Flow<Resource<TimezoneResponse>> {

        val googlePlacesApi = getGooglePlacesApi()
        val apiKey = App.getContext()?.resources?.getString(R.string.places_api_key)
            .getEmptyIfNull()
        val url =
            buildTimezoneApiUrl(latitude = latitude, longitude = longitude, placesApiKey = apiKey)
        return SafeApiCall.makeNetworkCall { googlePlacesApi.getTimezone(url = url) }
            .flowOn(Dispatchers.IO)

    }

    suspend fun getCountry(
        latitude: Double,
        longitude: Double
    ): Flow<Resource<DetectCountryResponse>> {

        val googlePlacesApi = getGooglePlacesApi()
        val apiKey = App.getContext()?.resources?.getString(R.string.places_api_key)
            .getEmptyIfNull()
        val url = buildUrlToDetectCountry(
            latitude = latitude, longitude = longitude, placesApiKey = apiKey
        )
        return SafeApiCall.makeNetworkCall { googlePlacesApi.detectCountry(url = url) }
            .flowOn(Dispatchers.IO)

    }

    private fun getGooglePlacesApi(): GooglePlacesApi {
        return Retrofit.Builder()
            .client(getOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(EndPoint.API.GOOGLE_PLACES_BASE_URL)
            .build()
            .create(GooglePlacesApi::class.java)
    }

    private fun getOkHttpClient(): OkHttpClient {

        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

    }

    private fun buildUrl(country: String, postalCode: String, placesApiKey: String): String {
        return "geocode/json?address=$country&components=postal_code:$postalCode&key=$placesApiKey"
    }

    private fun buildTimezoneApiUrl(
        latitude: Double,
        longitude: Double,
        placesApiKey: String
    ): String {
        return "timezone/json?location=$latitude,$longitude&timestamp=${System.currentTimeMillis() / 1000}&key=$placesApiKey"
    }

    private fun buildUrlToDetectCountry(
        latitude: Double,
        longitude: Double,
        placesApiKey: String
    ): String {
        return "geocode/json?latlng=$latitude,$longitude&key=$placesApiKey"
    }


}