package com.kash4me.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class CustomerDetailsFromMerchantResponse(
    val amount_left: String,
    val amount_spent: String,
    val cashback_amount: String,
    val created_at: String,
    val processing_amount: String,
    val total_earned: String,
    @SerializedName("qr_image")
    val qrImage: String?
) : Parcelable