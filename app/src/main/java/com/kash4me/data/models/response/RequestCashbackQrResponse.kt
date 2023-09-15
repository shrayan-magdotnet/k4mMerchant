package com.kash4me.data.models.response


import com.google.gson.annotations.SerializedName

data class RequestCashbackQrResponse(
    @SerializedName("qr_image")
    val qrImage: String? = ""
)