package com.kash4me.data.models.request


import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email")
    val email: String? = "",
    @SerializedName("user_name")
    val username: String? = "",
    @SerializedName("password")
    val password: String? = ""
)