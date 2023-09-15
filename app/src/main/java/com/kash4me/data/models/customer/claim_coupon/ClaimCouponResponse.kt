package com.kash4me.data.models.customer.claim_coupon


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ClaimCouponResponse(
    @SerializedName("data")
    val `data`: Data? = Data(),
    @SerializedName("message")
    val message: String? = ""
) {
    @Keep
    data class Data(
        @SerializedName("merchant_name")
        val merchantName: String? = "",
        @SerializedName("purchase_amount")
        val purchaseAmount: String? = ""
    )
}