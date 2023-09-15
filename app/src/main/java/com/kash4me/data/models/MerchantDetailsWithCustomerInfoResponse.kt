package com.kash4me.data.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class MerchantDetailsWithCustomerInfoResponse(
    @SerializedName("active_cashback_settings")
    val active_cashback_settings: ActiveCashbackSetting,
    @SerializedName("address")
    val address: String,
    @SerializedName("customer_id")
    val customer_id: Int,
    @SerializedName("description")
    val description: String,
    @SerializedName("distance")
    val distance: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("latest_txn")
    val latest_txn: LatestTxn?,
    @SerializedName("latitude")
    val latitude: String,
    @SerializedName("logo")
    val logo: Logo,
    @SerializedName("longitude")
    val longitude: String,
    @SerializedName("mobile_no")
    val mobile_no: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("promotional_text")
    val promotionalText: String? = null,
    @SerializedName("qr_image")
    val qrImage: String? = null,
    @SerializedName("unique_id")
    val uniqueId: String? = null
)

@Keep
data class LatestTxn(
    @SerializedName("amount_left")
    val amount_left: String?,
    @SerializedName("amount_spent")
    val amount_spent: String?,
    @SerializedName("cashback_amount")
    val cashback_amount: String?,
    @SerializedName("last_visit")
    val last_visit: String?,
    @SerializedName("processing_amount")
    val processing_amount: String?
)