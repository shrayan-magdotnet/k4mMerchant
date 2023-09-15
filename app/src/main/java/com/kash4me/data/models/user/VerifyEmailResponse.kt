package com.kash4me.data.models.user


import com.google.gson.annotations.SerializedName

data class VerifyEmailResponse(
    @SerializedName("access_token")
    val accessToken: String? = "",
    @SerializedName("refresh_token")
    val refreshToken: String? = "",
    @SerializedName("user_type")
    val userType: Int? = null
)