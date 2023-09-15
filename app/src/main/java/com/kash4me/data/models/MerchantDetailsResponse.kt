package com.kash4me.data.models

data class MerchantDetailsResponse(
    val active_cashback_settings: ActiveCashbackSettings,
    val address: String,
    val email: String?,
    val head_office_details: HeadOfficeDetails,
    val id: Int,
    val latitude: String,
    val logo: Logo,
    val longitude: String,
    val mobile_no: String,
    val name: String,
    val tags_details: List<TagsDetail>,
    val unique_office_id: String,
    val zip_code: String
)

