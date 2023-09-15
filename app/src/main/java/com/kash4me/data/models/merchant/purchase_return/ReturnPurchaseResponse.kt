package com.kash4me.data.models.merchant.purchase_return


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ReturnPurchaseResponse(
    @SerializedName("message")
    val message: String? = ""
)