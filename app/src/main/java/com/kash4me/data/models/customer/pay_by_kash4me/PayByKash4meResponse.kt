package com.kash4me.data.models.customer.pay_by_kash4me


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class PayByKash4meResponse(
    @SerializedName("date")
    val date: String? = "",
    @SerializedName("qr_image")
    val qrImage: String? = ""
) : Parcelable