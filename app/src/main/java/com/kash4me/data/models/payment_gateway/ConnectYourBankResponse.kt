package com.kash4me.data.models.payment_gateway


import com.google.gson.annotations.SerializedName

data class ConnectYourBankResponse(
    @SerializedName("iframe_url")
    val iframeUrl: String? = ""
)