package com.kash4me.data.models.merchant.update_profile


import com.google.gson.annotations.SerializedName
import com.kash4me.data.models.merchant.TagsDetail

data class MerchantProfileUpdateResponse(
//    @SerializedName("active_cashback_settings")
//    val activeCashbackSettings: ActiveCashbackSettings? = ActiveCashbackSettings(),
    @SerializedName("address")
    val address: String? = "",
    @SerializedName("country_name")
    val countryName: String? = "",
    @SerializedName("description")
    val description: String? = "",
    @SerializedName("email")
    val email: String? = "",
    @SerializedName("head_office_details")
    val headOfficeDetails: HeadOfficeDetails? = HeadOfficeDetails(),
    @SerializedName("head_office_id")
    val headOfficeId: String? = "",
    @SerializedName("id")
    val id: Int? = 0,
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
    @SerializedName("tags_details")
    val tagsDetails: List<TagsDetail?>? = listOf(),
    @SerializedName("timezone")
    val timezone: String? = "",
    @SerializedName("website")
    val website: String? = "",
    @SerializedName("unique_office_id")
    val uniqueOfficeId: String? = "",
    @SerializedName("zip_code")
    val zipCode: String? = ""
) {
    data class ActiveCashbackSettings(
        @SerializedName("cashback_amount")
        val cashbackAmount: String? = "",
        @SerializedName("cashback_percentage")
        val cashbackPercentage: String? = "",
        @SerializedName("cashback_type")
        val cashbackType: Int? = 0,
        @SerializedName("id")
        val id: Int? = 0,
        @SerializedName("maturity_amount")
        val maturityAmount: String? = ""
    )

    data class HeadOfficeDetails(
        @SerializedName("address")
        val address: String? = "",
        @SerializedName("head_merchant_id")
        val headMerchantId: String? = "",
        @SerializedName("mobile_no")
        val mobileNo: String? = "",
        @SerializedName("name")
        val name: String? = "",
        @SerializedName("unique_office_id")
        val uniqueOfficeId: String? = ""
    )

}