package com.kash4me.data.models.request


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class RequestCashbackQrRequest(
    @SerializedName("amount")
    val amount: Double? = 0.0,
    @SerializedName("shop_id")
    val shopId: Int? = 0
) : Parcelable