package com.kash4me.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

//data class MerchantResponse(
//    val active_cashback_settings: List<Any>,
//    val address: String,
//    val head_office_details: Any,
//    val id: Int,
//    val latitude: String,
//    val logo: Logo,
//    val longitude: String,
//    val mobile_no: String,
//    val name: String,
//    val tags_details: List<Any>,
//    val unique_office_id: String,
//    val zip_code: String
//)

@Parcelize
data class Logo(
    val full_size: String,
    val medium_square_crop: String
) : Parcelable