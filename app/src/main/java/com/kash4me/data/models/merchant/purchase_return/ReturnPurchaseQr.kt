package com.kash4me.data.models.merchant.purchase_return


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class ReturnPurchaseQr(
    @SerializedName("active_cashback_setting")
    val activeCashbackSettingsId: Int? = null,
//    @SerializedName("active_cashback_settings")
//    val activeCashbackSettings: ActiveCashbackSettings? = ActiveCashbackSettings(),
    @SerializedName("cashback_amount")
    val cashbackAmount: String? = "",
    @SerializedName("customer_id")
    val customerId: Int? = 0,
    @SerializedName("customer_name")
    val customerName: String? = "",
    @SerializedName("customer_unique_id")
    val customerUniqueId: String? = "",
    @SerializedName("merchant_id")
    val merchantId: Int? = 0,
    @SerializedName("purchase_amount")
    val purchaseAmount: String? = "",
    @SerializedName("txn_date")
    val txnDate: String? = "",
    @SerializedName("txn_id")
    val txnId: Int? = 0,
    @SerializedName("type")
    val type: String? = "",
    @SerializedName("qr_token")
    val qrToken: String? = ""
) : Parcelable {
    @Parcelize
    @Keep
    data class ActiveCashbackSettings(
        @SerializedName("cashback_amount")
        val cashbackAmount: String? = "",
        @SerializedName("cashback_percentage")
        val cashbackPercentage: String? = "",
        @SerializedName("cashback_type")
        val cashbackType: Int? = 0,
        @SerializedName("id")
        val id: Int? = 0,
        @SerializedName("maturity_amount")
        val maturityAmount: String? = ""
    ) : Parcelable
}