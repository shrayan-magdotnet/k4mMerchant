package com.kash4me.data.models.payment_gateway


import com.google.gson.annotations.SerializedName

data class PaymentGatewayResponse(
    @SerializedName("description")
    val description: String? = "",
    @SerializedName("id")
    val id: Int? = 0,
    @SerializedName("identifier")
    val identifier: String? = "",
    @SerializedName("name")
    val name: String? = ""
)