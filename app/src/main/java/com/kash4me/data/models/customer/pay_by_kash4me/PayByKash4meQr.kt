package com.kash4me.data.models.customer.pay_by_kash4me


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class PayByKash4meQr(
    @SerializedName("customer_id")
    val customerId: Int? = 0,
    @SerializedName("customer_name")
    val customerName: String? = "",
    @SerializedName("customer_unique_id")
    val customerUniqueId: String? = "",
    @SerializedName("purchase_amount")
    val purchaseAmount: String? = "",
    @SerializedName("qr_token")
    val qrToken: String? = "",
    @SerializedName("txn_date")
    val transactionDate: String? = "",
    @SerializedName("transaction_fee")
    val transactionFee: String? = "",
    @SerializedName("type")
    val type: String? = ""
) : Parcelable