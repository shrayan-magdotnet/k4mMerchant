package com.kash4me.data.models.customer.view_merchant_details


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class MerchantDetailsResponse(
    @SerializedName("active_cashback_settings")
    val activeCashbackSettings: ActiveCashbackSettings? = ActiveCashbackSettings(),
    @SerializedName("address")
    val address: String? = "",
    @SerializedName("announcement")
    val announcement: Announcement? = null,
    @SerializedName("customer_id")
    val customerId: Int? = 0,
    @SerializedName("description")
    val description: String? = "",
    @SerializedName("distance")
    val distance: String? = "",
    @SerializedName("id")
    val id: Int? = 0,
    @SerializedName("is_deleted")
    val isDeleted: Boolean? = false,
    @SerializedName("latest_txn")
    val latestTxn: LatestTxn? = LatestTxn(),
    @SerializedName("latitude")
    val latitude: String? = "",
    @SerializedName("logo")
    val logo: String? = "",
    @SerializedName("longitude")
    val longitude: String? = "",
    @SerializedName("main_image")
    val mainImage: String? = "",
    @SerializedName("mobile_no")
    val mobileNo: String? = "",
    @SerializedName("name")
    val name: String? = "",
    @SerializedName("promotional_text")
    val promotionalText: String? = "",
    @SerializedName("qr_image")
    val qrImage: String? = "",
    @SerializedName("unique_id")
    val uniqueId: String? = "",
    @SerializedName("website")
    val website: String? = ""
) {
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
    )

    @Keep
    data class LatestTxn(
        @SerializedName("amount_left")
        val amountLeft: String? = "",
        @SerializedName("amount_spent")
        val amountSpent: String? = "",
        @SerializedName("cashback_amount")
        val cashbackAmount: String? = "",
        @SerializedName("goal_amount")
        val goalAmount: String? = "",
        @SerializedName("last_visit")
        val lastVisit: String? = "",
        @SerializedName("processing_amount")
        val processingAmount: String? = "",
        @SerializedName("progress_percent")
        val progressPercent: String? = ""
    )

    @Keep
    data class Announcement(
        @SerializedName("announced_at")
        val announcedAt: String? = "",
        @SerializedName("expire_on")
        val expireOn: String? = "",
        @SerializedName("merchant_id")
        val merchantId: Int? = 0,
        @SerializedName("merchant_name")
        val merchantName: String? = "",
        @SerializedName("message")
        val message: String? = "",
        @SerializedName("notification_type")
        val notificationType: String? = ""
    )

}