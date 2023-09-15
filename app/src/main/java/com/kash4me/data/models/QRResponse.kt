package com.kash4me.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class QRResponse(
    @SerializedName("active_cashback_setting")
    val activeCashbackSettingsId: Int? = null,
//    val activeCashBackSetting: ActiveCashbackSetting?,
    @SerializedName("cashback_amount")
    val cashbackAmount: Double? = 0.0,
    @SerializedName("customer_id")
    val customerId: Int?,
    @SerializedName("customer_name")
    val customerName: String? = "",
    @SerializedName("customer_unique_id")
    val customerUniqueId: String? = "",
    @SerializedName("merchant_id")
    val merchantId: Int?,
    @SerializedName("purchase_amount")
    var purchaseAmount: String? = "0.00",
    var purchaseAmountInt: Double? = 0.0,
    @SerializedName("final_qr")
    var finalQR: Boolean? = false,
    val qrCodeInBase64: String?,
    @SerializedName("qr_token")
    val qrToken: String?
) : Parcelable