package com.kash4me.data.models.customer.update_profile


import com.google.gson.annotations.SerializedName

data class CustomerProfileUpdateRequest(
    @SerializedName("address")
    val address: String? = "",
    @SerializedName("latitude")
    val latitude: String? = "",
    @SerializedName("longitude")
    val longitude: String? = "",
    @SerializedName("mobile_no")
    val mobileNo: String? = "",
    @SerializedName("zip_code")
    val zipCode: String? = ""
)