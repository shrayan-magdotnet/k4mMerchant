package com.kash4me.data.models.merchant.delete


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class DeleteMerchantResponse(
    @SerializedName("data")
    val `data`: Data? = Data(),
    @SerializedName("message")
    val message: String? = ""
) {
    @Keep
    data class Data(
        @SerializedName("id")
        val id: Int? = 0
    )
}