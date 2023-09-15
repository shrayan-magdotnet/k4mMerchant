package com.kash4me.data.models.response


import com.google.gson.annotations.SerializedName

data class RefreshAccessTokenResponse(
    @SerializedName("access")
    val access: String? = ""
)