package com.kash4me.data.models.merchant.accept_kash4me_payment


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class AcceptKash4mePaymentResponse(
    @SerializedName("cashback_amount")
    val cashbackAmount: String? = "",
    @SerializedName("comission_percentage")
    val comissionPercentage: Double? = 0.0,
    @SerializedName("id")
    val id: Int? = 0,
    @SerializedName("payment_transaction_id")
    val paymentTransactionId: String? = "",
    @SerializedName("transaction_amount")
    val transactionAmount: String? = "",
    @SerializedName("txn_fee")
    val txnFee: Double? = 0.0
) : Parcelable