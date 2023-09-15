package com.kash4me.data.models.merchant.update_profile


import com.google.gson.annotations.SerializedName

data class MerchantProfileUpdateRequest(
    @SerializedName("address")
    val address: String? = "",
    @SerializedName("country")
    val countryName: String? = "",
    @SerializedName("description")
    val description: String? = "",
    @SerializedName("head_office_id")
    val headOfficeId: String? = "",
    @SerializedName("latitude")
    val latitude: String? = "",
    @SerializedName("logo")
    val logo: String? = "",
    @SerializedName("longitude")
    val longitude: String? = "",
    @SerializedName("mobile_no")
    val mobileNo: String? = "",
    @SerializedName("name")
    val name: String? = "",
    @SerializedName("promotional_text")
    val promotionalText: String? = "",
    @SerializedName("person_name")
    val personName: String? = "",
    @SerializedName("tags")
    val tags: List<String?>? = listOf(),
    @SerializedName("zip_code")
    val zipCode: String? = ""
)