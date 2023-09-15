package com.kash4me.data.models.merchant


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class CustomerDetailsResponseDto(
    @SerializedName("amount_left")
    val amountLeft: String? = "",
    @SerializedName("amount_spent")
    val amountSpent: String? = "",
    @SerializedName("cashback_amount")
    val cashbackAmount: String? = "",
    @SerializedName("created_at")
    val createdAt: String? = "",
    @SerializedName("processing_amount")
    val processingAmount: String? = "",
    @SerializedName("qr_image")
    val qrImage: String? = "",
    @SerializedName("total_earned")
    val totalEarned: String? = "",
    @SerializedName("user_details")
    val userDetails: UserDetails? = UserDetails()
) : Parcelable {
    @Keep
    @Parcelize
    data class UserDetails(
        @SerializedName("email")
        val email: String? = "",
        @SerializedName("id")
        val id: Int? = 0,
        @SerializedName("is_deleted")
        val isDeleted: Boolean? = false,
        @SerializedName("nick_name")
        val nickName: String? = "",
        @SerializedName("unique_id")
        val uniqueId: String? = ""
    ) : Parcelable
}