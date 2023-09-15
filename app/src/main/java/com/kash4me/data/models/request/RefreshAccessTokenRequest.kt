package com.kash4me.data.models.request


import com.google.gson.annotations.SerializedName

data class RefreshAccessTokenRequest(
    @SerializedName("refresh")
    val refresh: String? = ""
)