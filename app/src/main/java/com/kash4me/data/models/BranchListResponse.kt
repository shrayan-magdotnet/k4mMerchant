package com.kash4me.data.models

data class BranchListResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<Branch>?
)

data class ActiveCashbackSettings(
    val cashback_amount: String,
    val cashback_percentage: String,
    val cashback_type: Int,
    val id: Int,
    val maturity_amount: String
)

data class Branch(
    val active_cashback_settings: ActiveCashbackSettings,
    val address: String,
    val head_office_details: HeadOfficeDetails,
    val head_office_id: String,
    val id: Int,
    val latitude: String,
    val logo: String,
    val longitude: String,
    val mobile_no: String,
    val name: String,
    val tags_details: List<TagsDetail>,
    val unique_office_id: String,
    val zip_code: String
)

data class TagsDetail(
    val name: String,
    val tag_id: String
)