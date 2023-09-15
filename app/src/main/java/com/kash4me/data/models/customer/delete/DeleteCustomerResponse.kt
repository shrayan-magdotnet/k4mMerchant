package com.kash4me.data.models.customer.delete


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class DeleteCustomerResponse(
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