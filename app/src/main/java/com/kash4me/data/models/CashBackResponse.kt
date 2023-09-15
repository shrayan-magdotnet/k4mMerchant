package com.kash4me.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CashBackResponse(
    val allow_usr_flag: Boolean,
    val archive_flag: Boolean,
    val cashback_amount: String,
    val cashback_percentage: String,
    val cashback_type: Int,
    val cashback_type_display: String,
    val id: Int,
    val maturity_amount: String,
    val merchant_details: MerchantDetails
) : Parcelable

@Parcelize
data class MerchantDetails(
    val address: String,
    val id: Int,
//    val logo: Logo,
    val mobile_no: String,
    val name: String
) : Parcelable

//class Logo