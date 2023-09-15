package com.kash4me.data.models.user


import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("access_token")
    val accessToken: String? = "",
    @SerializedName("cb_settings")
    val cbSettings: Boolean? = false,
    @SerializedName("merchant_shop")
    val merchantShop: Boolean? = false,
    @SerializedName("refresh_token")
    val refreshToken: String? = "",
    @SerializedName("user_profile")
    val userProfile: Boolean? = false,
    @SerializedName("user_type")
    val userType: Int? = 0
) {

    fun isMerchant(): Boolean {
        return userType == UserType.MERCHANT.id
    }

    fun isCustomer(): Boolean {
        return userType == UserType.CUSTOMER.id
    }

    fun isStaff(): Boolean {
        return userType == UserType.STAFF.id
    }

}