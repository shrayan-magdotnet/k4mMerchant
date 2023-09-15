package com.kash4me.data.models.customer.transactions_according_to_merchant

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Result(
    val amount_spent: String,
    val cashback_amount: String,
    val date: String,
    val id: Int,
    val time: String,
    @SerializedName("timezone")
    val timezone: String,
    @SerializedName("transaction_type")
    val transactionType: String
) : Parcelable