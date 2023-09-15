package com.kash4me.data.models.merchant.registration


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class MerchantBasicInfoRequest(
    @SerializedName("basic_info")
    val basicInfo: BasicInfo? = BasicInfo(),
    @SerializedName("business_info")
    val businessInfo: BusinessInfo? = BusinessInfo(),
    @SerializedName("tags")
    val tags: List<String?>? = listOf()
) {
    @Keep
    data class BasicInfo(
        @SerializedName("name")
        val name: String? = ""
    )

    @Keep
    data class BusinessInfo(
        @SerializedName("address")
        val address: String? = "",
        @SerializedName("country")
        val country: String? = "",
        @SerializedName("description")
        val description: String? = "",
        @SerializedName("head_office_id")
        val headOfficeId: String? = "",
        @SerializedName("latitude")
        val latitude: String? = "",
        @SerializedName("longitude")
        val longitude: String? = "",
        @SerializedName("mobile_no")
        val mobileNo: String? = "",
        @SerializedName("name")
        val name: String? = "",
        @SerializedName("promotional_text")
        val promotionalText: String? = "",
        @SerializedName("timezone")
        val timezone: String? = "",
        @SerializedName("zip_code")
        val zipCode: String? = ""
    )
}