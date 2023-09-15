package com.kash4me.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BusinessInfoResponse(
    val basic_info: BasicInfo,
    val business_info: BusinessInfo,
    val tags_list: List<TagDetail>? = null
) : Parcelable

@Parcelize
data class BasicInfo(
    val merchant_user_id: Int,
    val mobile_no: String,
    val name: String
) : Parcelable

@Parcelize
data class BusinessInfo(
    val address: String,
    val country_name: String,
    val description: String,
    val head_office_details: HeadOfficeDetails,
    val latitude: String,
    val logo: String? = null,
    val longitude: String,
    val mobile_no: String,
    val name: String,
    val promotional_text: String,
    val shop_id: Int,
    val timezone: String,
    val website: String?,
    val unique_office_id: String,
    val zip_code: String
) : Parcelable

@Parcelize
data class HeadOfficeDetails(
    val address: String,
    val head_merchant_id: String,
    val mobile_no: String,
    val name: String,
    val unique_office_id: String
) : Parcelable

@Parcelize
data class TagDetail(
    val name: String,
    val tag_id: String
) : Parcelable