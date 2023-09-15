package com.kash4me.data.models.user.timezone


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class TimezoneResponse(
    @SerializedName("data")
    val availableTimezones: List<String?>? = listOf()
)