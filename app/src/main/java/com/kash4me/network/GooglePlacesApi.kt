package com.kash4me.network

import com.kash4me.data.models.places_api.DetectCountryResponse
import com.kash4me.data.models.places_api.ReverseGeocodeResponse
import com.kash4me.data.models.places_api.TimezoneResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface GooglePlacesApi {

    @GET
    suspend fun reverseGeocode(@Url url: String): Response<ReverseGeocodeResponse>

    @GET
    suspend fun getTimezone(@Url url: String): Response<TimezoneResponse>

    @GET
    suspend fun detectCountry(@Url url: String): Response<DetectCountryResponse>

}