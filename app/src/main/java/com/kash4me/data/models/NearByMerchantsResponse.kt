package com.kash4me.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class NearByMerchantsResponse(
    val count: Int,
    val next: String,
    val previous: String,
    val results: List<Merchant>
) : Parcelable


@Parcelize
data class Merchant(
    val active_cashback_settings: ActiveCashbackSetting,
    val address: String,
    val distance: String,
    val id: Int,
    @SerializedName("logo")
    val logo: String?,
    val mobile_no: String,
    val name: String,
    @SerializedName("is_deleted")
    val isDeleted: Boolean? = false,
    @SerializedName("promotional_text")
    val promotionalText: String? = ""
) : Parcelable


@Parcelize
data class ActiveCashbackSetting(
    val cashback_amount: String,
    val cashback_percentage: String,
    val cashback_type: Int,
    val id: Int,
    val maturity_amount: String
) : Parcelable


@Parcelize
data class Logos(
    val full_size: String,
    val medium_square_crop: String
) : Parcelable