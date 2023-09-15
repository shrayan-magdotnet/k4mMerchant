package com.kash4me.data.models.places_api


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class TimezoneResponse(
    @SerializedName("dstOffset")
    val dstOffset: Int? = 0,
    @SerializedName("rawOffset")
    val rawOffset: Int? = 0,
    @SerializedName("status")
    val status: String? = "",
    @SerializedName("timeZoneId")
    val timeZoneId: String? = "",
    @SerializedName("timeZoneName")
    val timeZoneName: String? = ""
)