package com.kash4me.data.models.customer.update_profile


import com.google.gson.annotations.SerializedName

data class CustomerProfileUpdateRequestV2(
    @SerializedName("address")
    val address: String? = "",
    @SerializedName("country")
    val country: String? = "",
    @SerializedName("date_of_birth")
    val dateOfBirth: String? = "",
    @SerializedName("latitude")
    val latitude: String? = "",
    @SerializedName("longitude")
    val longitude: String? = "",
    @SerializedName("mobile_no")
    val mobileNo: String? = "",
    @SerializedName("nick_name")
    val nickName: String? = "",
    @SerializedName("zip_code")
    val zipCode: String? = ""
)